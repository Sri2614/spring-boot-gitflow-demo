# 🔄 GitHub Actions Workflow Patterns - Advanced Guide

## Table of Contents
1. [Workflow Patterns Overview](#workflow-patterns-overview)
2. [Event Triggers Deep Dive](#event-triggers-deep-dive)
3. [Advanced Conditional Logic](#advanced-conditional-logic)
4. [Workflow Orchestration](#workflow-orchestration)
5. [Error Handling Patterns](#error-handling-patterns)
6. [Security Best Practices](#security-best-practices)
7. [Performance Optimization](#performance-optimization)
8. [Real-World Examples](#real-world-examples)

---

## Workflow Patterns Overview

### Pattern 1: Sequential Pipeline
Used for: Deployment pipelines where each stage depends on the previous

```yaml
name: Sequential Deployment
on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    outputs:
      version: ${{ steps.version.outputs.version }}
      artifact-id: ${{ steps.build.outputs.artifact-id }}
    steps:
      - uses: actions/checkout@v4
      - name: Build application
        id: build
        run: |
          # Build logic here
          echo "artifact-id=app-$(date +%s)" >> $GITHUB_OUTPUT

  test:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Run tests
        run: |
          echo "Testing artifact: ${{ needs.build.outputs.artifact-id }}"

  deploy-staging:
    needs: [build, test]
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - name: Deploy to staging
        run: |
          echo "Deploying ${{ needs.build.outputs.artifact-id }} to staging"

  integration-tests:
    needs: deploy-staging
    runs-on: ubuntu-latest
    steps:
      - name: Run integration tests
        run: |
          curl -f https://staging.example.com/health

  deploy-production:
    needs: [deploy-staging, integration-tests]
    runs-on: ubuntu-latest
    environment: production
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to production
        run: |
          echo "Deploying to production"
```

### Pattern 2: Parallel Execution with Fan-Out/Fan-In
Used for: Independent tasks that can run simultaneously

```yaml
name: Parallel Quality Checks
on:
  pull_request:
    branches: [main, develop]

jobs:
  changes:
    runs-on: ubuntu-latest
    outputs:
      backend: ${{ steps.changes.outputs.backend }}
      frontend: ${{ steps.changes.outputs.frontend }}
      docs: ${{ steps.changes.outputs.docs }}
    steps:
      - uses: actions/checkout@v4
      - uses: dorny/paths-filter@v2
        id: changes
        with:
          filters: |
            backend:
              - 'src/main/**'
              - 'pom.xml'
            frontend:
              - 'src/main/resources/static/**'
              - 'package.json'
            docs:
              - 'docs/**'
              - '*.md'

  backend-tests:
    needs: changes
    if: needs.changes.outputs.backend == 'true'
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [17, 21]
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java ${{ matrix.java-version }}
        uses: actions/setup-java@v3
        with:
          java-version: ${{ matrix.java-version }}
          distribution: 'temurin'
      - name: Run backend tests
        run: mvn test -Dtest=**/*Test

  frontend-tests:
    needs: changes
    if: needs.changes.outputs.frontend == 'true'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Run frontend tests
        run: npm test

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run security scan
        run: |
          # Security scanning logic
          echo "Security scan completed"

  code-quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run code quality analysis
        run: |
          # Code quality analysis
          echo "Code quality check completed"

  # Fan-in: Collect all results
  quality-gate:
    needs: [backend-tests, frontend-tests, security-scan, code-quality]
    if: always()
    runs-on: ubuntu-latest
    steps:
      - name: Check quality gate
        run: |
          echo "Backend tests: ${{ needs.backend-tests.result }}"
          echo "Frontend tests: ${{ needs.frontend-tests.result }}"
          echo "Security scan: ${{ needs.security-scan.result }}"
          echo "Code quality: ${{ needs.code-quality.result }}"
          
          if [[ "${{ contains(needs.*.result, 'failure') }}" == "true" ]]; then
            echo "Quality gate failed"
            exit 1
          fi
          echo "Quality gate passed"
```

### Pattern 3: Event-Driven Workflow Chain
Used for: Complex workflows triggered by different events

```yaml
name: Release Workflow Chain
on:
  issues:
    types: [opened, edited]
  pull_request:
    types: [closed]
  workflow_run:
    workflows: ["Build and Test"]
    types: [completed]
    branches: [main]

jobs:
  analyze-trigger:
    runs-on: ubuntu-latest
    outputs:
      trigger-type: ${{ steps.analyze.outputs.trigger-type }}
      action-needed: ${{ steps.analyze.outputs.action-needed }}
    steps:
      - name: Analyze trigger
        id: analyze
        run: |
          if [[ "${{ github.event_name }}" == "issues" ]]; then
            if [[ "${{ contains(github.event.issue.labels.*.name, 'release:major') }}" == "true" ]]; then
              echo "trigger-type=release-major" >> $GITHUB_OUTPUT
              echo "action-needed=create-release-branch" >> $GITHUB_OUTPUT
            elif [[ "${{ contains(github.event.issue.labels.*.name, 'release:minor') }}" == "true" ]]; then
              echo "trigger-type=release-minor" >> $GITHUB_OUTPUT
              echo "action-needed=create-release-branch" >> $GITHUB_OUTPUT
            fi
          elif [[ "${{ github.event_name }}" == "pull_request" ]] && [[ "${{ github.event.pull_request.merged }}" == "true" ]]; then
            echo "trigger-type=pr-merged" >> $GITHUB_OUTPUT
            if [[ "${{ github.event.pull_request.base.ref }}" == "main" ]]; then
              echo "action-needed=deploy-production" >> $GITHUB_OUTPUT
            fi
          elif [[ "${{ github.event_name }}" == "workflow_run" ]] && [[ "${{ github.event.workflow_run.conclusion }}" == "success" ]]; then
            echo "trigger-type=build-success" >> $GITHUB_OUTPUT
            echo "action-needed=deploy-staging" >> $GITHUB_OUTPUT
          fi

  create-release-branch:
    needs: analyze-trigger
    if: needs.analyze-trigger.outputs.action-needed == 'create-release-branch'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
          fetch-depth: 0
      - name: Create release branch
        run: |
          # Release branch creation logic
          echo "Creating release branch for ${{ needs.analyze-trigger.outputs.trigger-type }}"

  deploy-staging:
    needs: analyze-trigger
    if: needs.analyze-trigger.outputs.action-needed == 'deploy-staging'
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - name: Deploy to staging
        run: |
          echo "Deploying to staging environment"

  deploy-production:
    needs: analyze-trigger
    if: needs.analyze-trigger.outputs.action-needed == 'deploy-production'
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to production
        run: |
          echo "Deploying to production environment"
```

---

## Event Triggers Deep Dive

### Complex Event Filtering

```yaml
# Advanced push trigger with multiple conditions
on:
  push:
    branches:
      - main
      - 'release/**'
      - 'hotfix/**'
    paths:
      - 'src/**'
      - 'pom.xml'
      - '.github/workflows/**'
    paths-ignore:
      - 'docs/**'
      - '**.md'
      - '.gitignore'
    tags:
      - 'v*.*.*'

# Pull request with sophisticated filtering
on:
  pull_request:
    branches: [main, develop]
    types: [opened, synchronize, reopened, ready_for_review]
    paths:
      - 'src/**'
      - 'pom.xml'

# Issue-based automation
on:
  issues:
    types: [opened, labeled]
  issue_comment:
    types: [created]

# Scheduled workflows with multiple schedules
on:
  schedule:
    - cron: '0 2 * * 1-5'    # Weekdays at 2 AM UTC
    - cron: '0 6 * * 0'      # Sundays at 6 AM UTC
    - cron: '0 */4 * * *'    # Every 4 hours

# Manual triggers with complex inputs
on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        type: choice
        options:
          - development
          - testing
          - staging
          - production
        default: 'development'
      deploy-strategy:
        description: 'Deployment strategy'
        required: false
        type: choice
        options:
          - blue-green
          - canary
          - rolling
        default: 'rolling'
      force-deploy:
        description: 'Force deployment even if tests fail'
        required: false
        type: boolean
        default: false
      custom-tag:
        description: 'Custom tag for deployment'
        required: false
        type: string
```

### Event Context Analysis

```yaml
jobs:
  analyze-context:
    runs-on: ubuntu-latest
    outputs:
      should-deploy: ${{ steps.analyze.outputs.should-deploy }}
      target-environment: ${{ steps.analyze.outputs.target-environment }}
      deployment-strategy: ${{ steps.analyze.outputs.deployment-strategy }}
    steps:
      - name: Analyze event context
        id: analyze
        run: |
          echo "Event: ${{ github.event_name }}"
          echo "Actor: ${{ github.actor }}"
          echo "Repository: ${{ github.repository }}"
          echo "Ref: ${{ github.ref }}"
          echo "SHA: ${{ github.sha }}"
          
          # Complex decision logic
          SHOULD_DEPLOY="false"
          TARGET_ENV="development"
          STRATEGY="rolling"
          
          if [[ "${{ github.event_name }}" == "push" ]]; then
            if [[ "${{ github.ref }}" == "refs/heads/main" ]]; then
              SHOULD_DEPLOY="true"
              TARGET_ENV="production"
              STRATEGY="blue-green"
            elif [[ "${{ github.ref }}" == "refs/heads/develop" ]]; then
              SHOULD_DEPLOY="true"
              TARGET_ENV="development"
            elif [[ "${{ github.ref }}" =~ refs/heads/release/.* ]]; then
              SHOULD_DEPLOY="true"
              TARGET_ENV="staging"
              STRATEGY="canary"
            fi
          elif [[ "${{ github.event_name }}" == "workflow_dispatch" ]]; then
            SHOULD_DEPLOY="true"
            TARGET_ENV="${{ github.event.inputs.environment }}"
            STRATEGY="${{ github.event.inputs.deploy-strategy }}"
          fi
          
          echo "should-deploy=$SHOULD_DEPLOY" >> $GITHUB_OUTPUT
          echo "target-environment=$TARGET_ENV" >> $GITHUB_OUTPUT
          echo "deployment-strategy=$STRATEGY" >> $GITHUB_OUTPUT
```

---

## Advanced Conditional Logic

### Multi-Condition Checks

```yaml
jobs:
  conditional-job:
    runs-on: ubuntu-latest
    if: >
      (github.event_name == 'push' && github.ref == 'refs/heads/main') ||
      (github.event_name == 'pull_request' && github.event.pull_request.base.ref == 'main') ||
      (github.event_name == 'workflow_dispatch' && github.event.inputs.force-run == 'true')
    steps:
      - name: Complex condition step
        if: >
          success() &&
          !contains(github.event.head_commit.message, '[skip ci]') &&
          (github.actor != 'dependabot[bot]' || contains(github.event.pull_request.labels.*.name, 'auto-merge'))
        run: echo "Conditions met"

  environment-specific:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        environment: [dev, staging, prod]
        include:
          - environment: dev
            runner: ubuntu-latest
            timeout: 5
          - environment: staging
            runner: ubuntu-latest
            timeout: 10
          - environment: prod
            runner: ubuntu-latest
            timeout: 15
        exclude:
          - environment: prod
    if: >
      (matrix.environment == 'dev') ||
      (matrix.environment == 'staging' && github.ref == 'refs/heads/develop') ||
      (matrix.environment == 'prod' && github.ref == 'refs/heads/main')
    steps:
      - name: Deploy to ${{ matrix.environment }}
        timeout-minutes: ${{ matrix.timeout }}
        run: echo "Deploying to ${{ matrix.environment }}"
```

### Dynamic Job Generation

```yaml
jobs:
  generate-matrix:
    runs-on: ubuntu-latest
    outputs:
      matrix: ${{ steps.set-matrix.outputs.matrix }}
    steps:
      - uses: actions/checkout@v4
      - name: Generate matrix
        id: set-matrix
        run: |
          # Generate matrix based on changed files
          if [[ "${{ contains(github.event.head_commit.modified, 'backend/') }}" == "true" ]]; then
            SERVICES+="backend,"
          fi
          if [[ "${{ contains(github.event.head_commit.modified, 'frontend/') }}" == "true" ]]; then
            SERVICES+="frontend,"
          fi
          if [[ "${{ contains(github.event.head_commit.modified, 'api/') }}" == "true" ]]; then
            SERVICES+="api,"
          fi
          
          SERVICES=${SERVICES%,}  # Remove trailing comma
          
          if [[ -z "$SERVICES" ]]; then
            MATRIX='{"service": []}'
          else
            MATRIX="{\"service\": [\"$(echo $SERVICES | sed 's/,/", "/g')\"]}"
          fi
          
          echo "matrix=$MATRIX" >> $GITHUB_OUTPUT

  test-services:
    needs: generate-matrix
    if: needs.generate-matrix.outputs.matrix != '{"service": []}'
    strategy:
      matrix: ${{ fromJSON(needs.generate-matrix.outputs.matrix) }}
    runs-on: ubuntu-latest
    steps:
      - name: Test ${{ matrix.service }}
        run: echo "Testing service: ${{ matrix.service }}"
```

---

## Workflow Orchestration

### Workflow Dependencies

```yaml
name: Complex Orchestration
on:
  workflow_run:
    workflows:
      - "Build and Test"
      - "Security Scan"
    types: [completed]
    branches: [main]

jobs:
  check-prerequisites:
    runs-on: ubuntu-latest
    outputs:
      build-passed: ${{ steps.check.outputs.build-passed }}
      security-passed: ${{ steps.check.outputs.security-passed }}
      can-deploy: ${{ steps.check.outputs.can-deploy }}
    steps:
      - name: Check workflow results
        id: check
        run: |
          # Check if both workflows passed
          BUILD_PASSED="false"
          SECURITY_PASSED="false"
          
          if [[ "${{ github.event.workflow_run.name }}" == "Build and Test" ]] && [[ "${{ github.event.workflow_run.conclusion }}" == "success" ]]; then
            BUILD_PASSED="true"
          fi
          
          if [[ "${{ github.event.workflow_run.name }}" == "Security Scan" ]] && [[ "${{ github.event.workflow_run.conclusion }}" == "success" ]]; then
            SECURITY_PASSED="true"
          fi
          
          # Check if we can deploy (both must be true)
          CAN_DEPLOY="false"
          if [[ "$BUILD_PASSED" == "true" ]] && [[ "$SECURITY_PASSED" == "true" ]]; then
            CAN_DEPLOY="true"
          fi
          
          echo "build-passed=$BUILD_PASSED" >> $GITHUB_OUTPUT
          echo "security-passed=$SECURITY_PASSED" >> $GITHUB_OUTPUT
          echo "can-deploy=$CAN_DEPLOY" >> $GITHUB_OUTPUT

  deploy:
    needs: check-prerequisites
    if: needs.check-prerequisites.outputs.can-deploy == 'true'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy application
        run: echo "Deploying application"
```

### Cross-Repository Workflows

```yaml
name: Trigger Downstream
on:
  push:
    branches: [main]

jobs:
  trigger-dependent-repos:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        repo:
          - owner/api-service
          - owner/frontend-app
          - owner/mobile-app
    steps:
      - name: Trigger workflow in ${{ matrix.repo }}
        uses: actions/github-script@v6
        with:
          github-token: ${{ secrets.CROSS_REPO_TOKEN }}
          script: |
            const [owner, repo] = '${{ matrix.repo }}'.split('/');
            
            try {
              const result = await github.rest.actions.createWorkflowDispatch({
                owner: owner,
                repo: repo,
                workflow_id: 'update-dependencies.yml',
                ref: 'main',
                inputs: {
                  'trigger-repo': '${{ github.repository }}',
                  'trigger-sha': '${{ github.sha }}',
                  'trigger-actor': '${{ github.actor }}'
                }
              });
              
              console.log(`Triggered workflow in ${owner}/${repo}`);
            } catch (error) {
              console.error(`Failed to trigger workflow in ${owner}/${repo}:`, error);
            }
```

---

## Error Handling Patterns

### Comprehensive Error Handling

```yaml
jobs:
  resilient-job:
    runs-on: ubuntu-latest
    steps:
      - name: Step with retry logic
        id: retry-step
        uses: nick-fields/retry@v2
        with:
          timeout_minutes: 10
          max_attempts: 3
          retry_wait_seconds: 30
          command: |
            # Your command that might fail
            curl -f https://api.example.com/health || exit 1

      - name: Step with fallback
        run: |
          # Primary action
          if ! primary_command; then
            echo "Primary command failed, trying fallback"
            if ! fallback_command; then
              echo "Both primary and fallback failed"
              # Send notification
              curl -X POST "${{ secrets.SLACK_WEBHOOK }}" \
                -H 'Content-type: application/json' \
                --data '{"text":"Deployment failed in ${{ github.repository }}"}'
              exit 1
            fi
          fi

      - name: Cleanup on failure
        if: failure()
        run: |
          # Cleanup resources on failure
          docker system prune -f
          kubectl delete namespace temp-${{ github.run_id }} --ignore-not-found

      - name: Always run cleanup
        if: always()
        run: |
          # Always cleanup temporary files
          rm -rf temp-*
          
      - name: Success notification
        if: success()
        run: |
          curl -X POST "${{ secrets.SLACK_WEBHOOK }}" \
            -H 'Content-type: application/json' \
            --data '{"text":"✅ Deployment successful in ${{ github.repository }}"}'
```

### Circuit Breaker Pattern

```yaml
jobs:
  circuit-breaker:
    runs-on: ubuntu-latest
    steps:
      - name: Check system health
        id: health-check
        run: |
          # Check if system is healthy enough to proceed
          FAILURE_COUNT=$(gh api repos/${{ github.repository }}/actions/runs \
            --jq '[.workflow_runs[] | select(.conclusion == "failure" and .created_at > (now - 3600))] | length')
          
          if [[ $FAILURE_COUNT -gt 5 ]]; then
            echo "Circuit breaker activated - too many failures in last hour"
            echo "failure-count=$FAILURE_COUNT" >> $GITHUB_OUTPUT
            exit 1
          fi
          
          echo "failure-count=$FAILURE_COUNT" >> $GITHUB_OUTPUT

      - name: Proceed with deployment
        if: steps.health-check.outputs.failure-count <= '5'
        run: |
          echo "System healthy, proceeding with deployment"
```

---

## Performance Optimization

### Parallel Execution Optimization

```yaml
jobs:
  optimize-parallel:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      max-parallel: 4
      matrix:
        test-group:
          - unit
          - integration
          - e2e
          - performance
        java-version: [17, 21]
        exclude:
          - test-group: performance
            java-version: 17
    steps:
      - name: Run ${{ matrix.test-group }} tests with Java ${{ matrix.java-version }}
        run: |
          case "${{ matrix.test-group }}" in
            "unit")
              mvn test -Dtest=**/*UnitTest
              ;;
            "integration")
              mvn test -Dtest=**/*IntegrationTest
              ;;
            "e2e")
              mvn test -Dtest=**/*E2ETest
              ;;
            "performance")
              mvn test -Dtest=**/*PerformanceTest
              ;;
          esac
```

### Advanced Caching Strategies

```yaml
jobs:
  advanced-caching:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      # Multi-level Maven cache
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.m2/repository
            ~/.m2/wrapper
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml', '.mvn/wrapper/maven-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
            ${{ runner.os }}-maven-

      # Docker layer caching
      - name: Cache Docker layers
        uses: actions/cache@v3
        with:
          path: /tmp/.buildx-cache
          key: ${{ runner.os }}-buildx-${{ github.sha }}
          restore-keys: |
            ${{ runner.os }}-buildx-

      # Node modules caching (if applicable)
      - name: Cache Node modules
        if: hashFiles('package-lock.json') != ''
        uses: actions/cache@v3
        with:
          path: ~/.npm
          key: ${{ runner.os }}-node-${{ hashFiles('**/package-lock.json') }}
          restore-keys: |
            ${{ runner.os }}-node-

      # Custom application cache
      - name: Cache application artifacts
        uses: actions/cache@v3
        with:
          path: |
            target/classes
            target/generated-sources
          key: ${{ runner.os }}-app-${{ hashFiles('src/**/*.java', 'src/**/*.properties') }}
```

This comprehensive guide covers advanced GitHub Actions patterns. Each pattern includes real-world scenarios, performance considerations, and best practices for enterprise-grade CI/CD implementations.