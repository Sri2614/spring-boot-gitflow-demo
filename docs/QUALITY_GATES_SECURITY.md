# 🎯 Quality Gates & Security - Enterprise Guide

## Table of Contents
1. [Quality Gates Framework](#quality-gates-framework)
2. [Security Scanning Deep Dive](#security-scanning-deep-dive)
3. [Code Quality Metrics](#code-quality-metrics)
4. [Testing Strategies](#testing-strategies)
5. [Performance Testing](#performance-testing)
6. [Compliance & Governance](#compliance--governance)
7. [Monitoring & Alerting](#monitoring--alerting)
8. [Best Practices](#best-practices)

---

## Quality Gates Framework

### Multi-Stage Quality Pipeline

```yaml
name: Comprehensive Quality Gates
on:
  pull_request:
    branches: [main, develop]

jobs:
  # Stage 1: Fast Feedback (< 2 minutes)
  fast-checks:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        check: [compile, lint, format, basic-security]
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run ${{ matrix.check }}
        run: |
          case "${{ matrix.check }}" in
            "compile")
              mvn compile -B -q
              ;;
            "lint")
              mvn checkstyle:check -B
              ;;
            "format")
              mvn spotless:check -B
              ;;
            "basic-security")
              mvn dependency-check:check -DskipTestScope=true -B
              ;;
          esac

  # Stage 2: Comprehensive Testing (2-8 minutes)
  comprehensive-tests:
    needs: fast-checks
    runs-on: ubuntu-latest
    strategy:
      matrix:
        test-type: [unit, integration, contract, mutation]
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run ${{ matrix.test-type }} tests
        run: |
          case "${{ matrix.test-type }}" in
            "unit")
              mvn test -Dtest=**/*Test -DexcludeGroups="integration,contract" \
                -Djacoco.skip=false -B
              ;;
            "integration")
              mvn test -Dtest=**/*IT -Dgroups="integration" \
                -Dtestcontainers.reuse.enable=true -B
              ;;
            "contract")
              mvn test -Dtest=**/*Contract* -Dgroups="contract" -B
              ;;
            "mutation")
              mvn org.pitest:pitest-maven:mutationCoverage \
                -DwithHistory=true -DhistoryInputFile=target/pit-history.txt -B
              ;;
          esac

      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results-${{ matrix.test-type }}
          path: |
            target/surefire-reports/
            target/failsafe-reports/
            target/pit-reports/

  # Stage 3: Deep Analysis (5-15 minutes)
  deep-analysis:
    needs: comprehensive-tests
    runs-on: ubuntu-latest
    strategy:
      matrix:
        analysis: [sonarqube, owasp-full, architecture, performance]
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for better analysis

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Download test artifacts
        uses: actions/download-artifact@v3
        with:
          path: target/

      - name: Run ${{ matrix.analysis }}
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          case "${{ matrix.analysis }}" in
            "sonarqube")
              mvn sonar:sonar \
                -Dsonar.projectKey=spring-boot-gitflow-demo \
                -Dsonar.organization=${{ github.repository_owner }} \
                -Dsonar.host.url=https://sonarcloud.io \
                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                -Dsonar.qualitygate.wait=true \
                -B
              ;;
            "owasp-full")
              mvn org.owasp:dependency-check-maven:aggregate \
                -DfailBuildOnCVSS=7.0 \
                -DsuppressionFile=owasp-suppressions.xml \
                -Dformats=HTML,XML,JSON,JUNIT \
                -B
              ;;
            "architecture")
              mvn test -Dtest=**/*ArchTest -B
              ;;
            "performance")
              mvn test -Dtest=**/*PerformanceTest \
                -Djmh.benchmarkMode=Throughput \
                -Djmh.outputTimeUnit=SECONDS \
                -B
              ;;
          esac

  # Stage 4: Quality Gate Decision
  quality-gate:
    needs: [fast-checks, comprehensive-tests, deep-analysis]
    if: always()
    runs-on: ubuntu-latest
    outputs:
      passed: ${{ steps.gate.outputs.passed }}
      score: ${{ steps.gate.outputs.score }}
    steps:
      - name: Evaluate quality gate
        id: gate
        run: |
          # Check if all jobs passed
          FAST_CHECKS_RESULT="${{ needs.fast-checks.result }}"
          COMPREHENSIVE_TESTS_RESULT="${{ needs.comprehensive-tests.result }}"
          DEEP_ANALYSIS_RESULT="${{ needs.deep-analysis.result }}"
          
          echo "Fast checks: $FAST_CHECKS_RESULT"
          echo "Comprehensive tests: $COMPREHENSIVE_TESTS_RESULT"
          echo "Deep analysis: $DEEP_ANALYSIS_RESULT"
          
          PASSED="false"
          SCORE=0
          
          # Calculate score based on results
          if [[ "$FAST_CHECKS_RESULT" == "success" ]]; then
            SCORE=$((SCORE + 30))
          fi
          
          if [[ "$COMPREHENSIVE_TESTS_RESULT" == "success" ]]; then
            SCORE=$((SCORE + 40))
          fi
          
          if [[ "$DEEP_ANALYSIS_RESULT" == "success" ]]; then
            SCORE=$((SCORE + 30))
          fi
          
          # Pass if score >= 80
          if [[ $SCORE -ge 80 ]]; then
            PASSED="true"
          fi
          
          echo "passed=$PASSED" >> $GITHUB_OUTPUT
          echo "score=$SCORE" >> $GITHUB_OUTPUT
          echo "Quality Gate Score: $SCORE/100"

      - name: Report quality gate result
        uses: actions/github-script@v6
        with:
          script: |
            const passed = '${{ steps.gate.outputs.passed }}' === 'true';
            const score = '${{ steps.gate.outputs.score }}';
            
            const icon = passed ? '✅' : '❌';
            const status = passed ? 'PASSED' : 'FAILED';
            
            const comment = `
            ## ${icon} Quality Gate ${status}
            
            **Score**: ${score}/100
            
            ### Stage Results
            - **Fast Checks**: ${{ needs.fast-checks.result }}
            - **Comprehensive Tests**: ${{ needs.comprehensive-tests.result }}
            - **Deep Analysis**: ${{ needs.deep-analysis.result }}
            
            ### Next Steps
            ${passed ? 
              '🎉 All quality gates passed! Ready for review.' : 
              '🔧 Quality issues detected. Please address them before proceeding.'}
            `;
            
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: comment
            });
```

### Quality Metrics Configuration

```yaml
# sonar-project.properties
sonar.projectKey=spring-boot-gitflow-demo
sonar.organization=sri2614
sonar.host.url=https://sonarcloud.io

# Coverage settings
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports

# Quality gates
sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300

# Code analysis settings
sonar.java.source=21
sonar.java.target=21
sonar.java.libraries=target/dependency/*.jar
sonar.java.test.libraries=target/test-classes

# Exclusions
sonar.exclusions=**/target/**,**/generated/**,**/*Test.java
sonar.test.exclusions=**/src/main/**

# Thresholds
sonar.coverage.overall.line.threshold=80
sonar.duplicated_lines_density.threshold=3
sonar.maintainability_rating.threshold=A
sonar.reliability_rating.threshold=A
sonar.security_rating.threshold=A
```

---

## Security Scanning Deep Dive

### Multi-Layer Security Analysis

```yaml
name: Security Analysis Pipeline
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: '0 2 * * *'  # Daily security scan

jobs:
  # Layer 1: Dependency Scanning
  dependency-security:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        scanner: [owasp, snyk, github-advisory]
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run ${{ matrix.scanner }}
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          case "${{ matrix.scanner }}" in
            "owasp")
              mvn org.owasp:dependency-check-maven:check \
                -DfailBuildOnCVSS=7.0 \
                -DcveValidForHours=24 \
                -DsuppressionFile=owasp-suppressions.xml \
                -Dformat=ALL \
                -B
              ;;
            "snyk")
              npm install -g snyk
              snyk auth $SNYK_TOKEN
              snyk test --severity-threshold=medium
              snyk monitor
              ;;
            "github-advisory")
              # GitHub Advisory Database check
              curl -H "Authorization: token $GITHUB_TOKEN" \
                "https://api.github.com/repos/${{ github.repository }}/vulnerability-alerts" | \
                jq '.[] | select(.state == "open")'
              ;;
          esac

  # Layer 2: Static Code Analysis
  static-analysis:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        tool: [sonarcloud, codeql, spotbugs, pmd]
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Initialize CodeQL
        if: matrix.tool == 'codeql'
        uses: github/codeql-action/init@v2
        with:
          languages: java
          queries: security-and-quality

      - name: Run ${{ matrix.tool }}
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          case "${{ matrix.tool }}" in
            "sonarcloud")
              mvn sonar:sonar \
                -Dsonar.projectKey=spring-boot-gitflow-demo \
                -Dsonar.organization=${{ github.repository_owner }} \
                -Dsonar.host.url=https://sonarcloud.io \
                -Dsonar.security.hotspots.inheritFromParent=true \
                -B
              ;;
            "codeql")
              mvn compile -B
              ;;
            "spotbugs")
              mvn com.github.spotbugs:spotbugs-maven-plugin:check \
                -DfailOnError=true \
                -DincludeFilterFile=spotbugs-security-include.xml \
                -B
              ;;
            "pmd")
              mvn pmd:pmd pmd:cpd \
                -Dpmd.rulesets=rulesets/java/quickstart.xml,category/java/security.xml \
                -DfailOnViolation=true \
                -B
              ;;
          esac

      - name: Perform CodeQL Analysis
        if: matrix.tool == 'codeql'
        uses: github/codeql-action/analyze@v2

  # Layer 3: Container Security
  container-security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker image
        run: |
          docker build -t spring-boot-app:${{ github.sha }} .

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'spring-boot-app:${{ github.sha }}'
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH,MEDIUM'

      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v2
        if: always()
        with:
          sarif_file: 'trivy-results.sarif'

      - name: Docker Scout
        uses: docker/scout-action@v1
        with:
          command: cves
          image: spring-boot-app:${{ github.sha }}
          only-severities: critical,high
          exit-code: true

  # Layer 4: Dynamic Security Testing
  dast:
    runs-on: ubuntu-latest
    needs: container-security
    steps:
      - uses: actions/checkout@v4
      - name: Start application
        run: |
          docker run -d -p 8080:8080 --name test-app \
            spring-boot-app:${{ github.sha }}
          
          # Wait for application to start
          timeout 60 bash -c 'while ! curl -f http://localhost:8080/actuator/health; do sleep 5; done'

      - name: OWASP ZAP Full Scan
        uses: zaproxy/action-full-scan@v0.4.0
        with:
          target: 'http://localhost:8080'
          rules_file_name: '.zap/rules.tsv'
          cmd_options: '-a'
          fail_action: true

      - name: Nuclei scan
        uses: projectdiscovery/nuclei-action@main
        with:
          target: 'http://localhost:8080'
          templates: 'vulnerabilities,misconfiguration'

      - name: Cleanup
        if: always()
        run: docker stop test-app && docker rm test-app

  # Security Report Aggregation
  security-report:
    needs: [dependency-security, static-analysis, container-security, dast]
    if: always()
    runs-on: ubuntu-latest
    steps:
      - name: Download security artifacts
        uses: actions/download-artifact@v3
        
      - name: Generate security report
        run: |
          echo "# Security Analysis Report" > security-report.md
          echo "Generated: $(date)" >> security-report.md
          echo "" >> security-report.md
          
          # Aggregate results from all scanners
          echo "## Dependency Security: ${{ needs.dependency-security.result }}" >> security-report.md
          echo "## Static Analysis: ${{ needs.static-analysis.result }}" >> security-report.md
          echo "## Container Security: ${{ needs.container-security.result }}" >> security-report.md
          echo "## Dynamic Testing: ${{ needs.dast.result }}" >> security-report.md
          
          # Upload comprehensive report
          cat security-report.md

      - name: Upload security report
        uses: actions/upload-artifact@v3
        with:
          name: security-report
          path: security-report.md
```

---

## Code Quality Metrics

### Comprehensive Quality Measurement

```yaml
# Maven configuration for quality plugins (pom.xml excerpt)
<properties>
    <!-- Quality thresholds -->
    <jacoco.line.coverage>0.80</jacoco.line.coverage>
    <jacoco.branch.coverage>0.75</jacoco.branch.coverage>
    <sonar.coverage.exclusions>
        **/config/**,
        **/dto/**,
        **/exception/**,
        **/*Application.java
    </sonar.coverage.exclusions>
    
    <!-- Mutation testing -->
    <pitest.mutationThreshold>75</pitest.mutationThreshold>
    <pitest.coverageThreshold>80</pitest.coverageThreshold>
    
    <!-- Code style -->
    <checkstyle.configLocation>checkstyle.xml</checkstyle.configLocation>
    <spotbugs.effort>Max</spotbugs.effort>
    <spotbugs.threshold>Medium</spotbugs.threshold>
</properties>

<plugins>
    <!-- JaCoCo Coverage -->
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.8</version>
        <configuration>
            <rules>
                <rule>
                    <element>BUNDLE</element>
                    <limits>
                        <limit>
                            <counter>LINE</counter>
                            <value>COVEREDRATIO</value>
                            <minimum>${jacoco.line.coverage}</minimum>
                        </limit>
                        <limit>
                            <counter>BRANCH</counter>
                            <value>COVEREDRATIO</value>
                            <minimum>${jacoco.branch.coverage}</minimum>
                        </limit>
                    </limits>
                </rule>
            </rules>
        </configuration>
    </plugin>

    <!-- Mutation Testing -->
    <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.9.8</version>
        <configuration>
            <mutationThreshold>${pitest.mutationThreshold}</mutationThreshold>
            <coverageThreshold>${pitest.coverageThreshold}</coverageThreshold>
            <excludedClasses>
                <param>*.*Application</param>
                <param>*.config.*</param>
                <param>*.dto.*</param>
            </excludedClasses>
            <targetTests>
                <param>*Test</param>
                <param>*Tests</param>
            </targetTests>
        </configuration>
    </plugin>

    <!-- Checkstyle -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-checkstyle-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
            <configLocation>${checkstyle.configLocation}</configLocation>
            <encoding>UTF-8</encoding>
            <consoleOutput>true</consoleOutput>
            <failsOnError>true</failsOnError>
            <linkXRef>false</linkXRef>
        </configuration>
    </plugin>

    <!-- SpotBugs -->
    <plugin>
        <groupId>com.github.spotbugs</groupId>
        <artifactId>spotbugs-maven-plugin</artifactId>
        <version>4.7.3.0</version>
        <configuration>
            <effort>${spotbugs.effort}</effort>
            <threshold>${spotbugs.threshold}</threshold>
            <xmlOutput>true</xmlOutput>
            <includeFilterFile>spotbugs-security-include.xml</includeFilterFile>
        </configuration>
    </plugin>
</plugins>
```

### Quality Dashboard Integration

```yaml
name: Quality Dashboard Update
on:
  push:
    branches: [main]
  schedule:
    - cron: '0 */6 * * *'  # Every 6 hours

jobs:
  quality-metrics:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Generate quality metrics
        run: |
          # Run all quality checks
          mvn clean test jacoco:report \
            checkstyle:check spotbugs:check pmd:check \
            org.pitest:pitest-maven:mutationCoverage \
            -B

      - name: Extract metrics
        id: metrics
        run: |
          # Extract coverage percentage
          COVERAGE=$(xmllint --xpath "string(//report/counter[@type='LINE']/@covered div //report/counter[@type='LINE']/@missed + //report/counter[@type='LINE']/@covered)" target/site/jacoco/jacoco.xml | bc -l)
          
          # Extract mutation score
          MUTATION_SCORE=$(xmllint --xpath "string(//mutations/@mutationScore)" target/pit-reports/mutations.xml)
          
          # Count violations
          CHECKSTYLE_VIOLATIONS=$(xmllint --xpath "count(//error)" target/checkstyle-result.xml)
          SPOTBUGS_VIOLATIONS=$(xmllint --xpath "count(//BugInstance)" target/spotbugsXml.xml)
          
          # Calculate quality score
          QUALITY_SCORE=$(echo "scale=2; ($COVERAGE * 100 + $MUTATION_SCORE - $CHECKSTYLE_VIOLATIONS - $SPOTBUGS_VIOLATIONS * 2) / 2" | bc)
          
          echo "coverage=$COVERAGE" >> $GITHUB_OUTPUT
          echo "mutation-score=$MUTATION_SCORE" >> $GITHUB_OUTPUT
          echo "checkstyle-violations=$CHECKSTYLE_VIOLATIONS" >> $GITHUB_OUTPUT
          echo "spotbugs-violations=$SPOTBUGS_VIOLATIONS" >> $GITHUB_OUTPUT
          echo "quality-score=$QUALITY_SCORE" >> $GITHUB_OUTPUT

      - name: Update quality badge
        uses: schneegans/dynamic-badges-action@v1.4.0
        with:
          auth: ${{ secrets.GIST_SECRET }}
          gistID: your-gist-id
          filename: quality-badge.json
          label: Quality Score
          message: ${{ steps.metrics.outputs.quality-score }}%
          color: ${{ steps.metrics.outputs.quality-score > 80 && 'brightgreen' || steps.metrics.outputs.quality-score > 60 && 'yellow' || 'red' }}

      - name: Send quality report
        uses: actions/github-script@v6
        with:
          script: |
            const coverage = '${{ steps.metrics.outputs.coverage }}';
            const mutationScore = '${{ steps.metrics.outputs.mutation-score }}';
            const checkstyleViolations = '${{ steps.metrics.outputs.checkstyle-violations }}';
            const spotbugsViolations = '${{ steps.metrics.outputs.spotbugs-violations }}';
            const qualityScore = '${{ steps.metrics.outputs.quality-score }}';
            
            const report = `
            ## 📊 Quality Metrics Report
            
            | Metric | Value | Threshold | Status |
            |--------|--------|-----------|--------|
            | Code Coverage | ${(coverage * 100).toFixed(1)}% | 80% | ${coverage >= 0.8 ? '✅' : '❌'} |
            | Mutation Score | ${mutationScore}% | 75% | ${mutationScore >= 75 ? '✅' : '❌'} |
            | Style Violations | ${checkstyleViolations} | 0 | ${checkstyleViolations == 0 ? '✅' : '❌'} |
            | Security Issues | ${spotbugsViolations} | 0 | ${spotbugsViolations == 0 ? '✅' : '❌'} |
            | **Overall Score** | **${qualityScore}%** | **80%** | **${qualityScore >= 80 ? '✅' : '❌'}** |
            
            Generated: ${new Date().toISOString()}
            `;
            
            // Create or update issue with quality report
            const issues = await github.rest.issues.listForRepo({
              owner: context.repo.owner,
              repo: context.repo.repo,
              labels: ['quality-report'],
              state: 'open'
            });
            
            if (issues.data.length > 0) {
              // Update existing issue
              await github.rest.issues.createComment({
                owner: context.repo.owner,
                repo: context.repo.repo,
                issue_number: issues.data[0].number,
                body: report
              });
            } else {
              // Create new issue
              await github.rest.issues.create({
                owner: context.repo.owner,
                repo: context.repo.repo,
                title: 'Quality Metrics Report',
                body: report,
                labels: ['quality-report']
              });
            }
```

This comprehensive guide continues with testing strategies, performance testing, compliance, and more advanced topics. Would you like me to continue with the remaining sections?