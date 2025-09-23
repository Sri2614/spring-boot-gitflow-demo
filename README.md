# Spring Boot GitFlow Demo 🚀

A comprehensive demonstration of GitHub workflows, GitFlow branching strategy, and CI/CD pipelines using Spring Boot.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![Docker](https://img.shields.io/badge/Docker-enabled-blue)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-enabled-green)

## 🎯 Project Overview

This project demonstrates enterprise-level software development practices including:

- **GitFlow Branching Strategy** - Structured branch management
- **Automated Release Management** - Issue-driven releases
- **Multi-Environment Deployments** - DEV → TST → UAT → PREPROD → PROD
- **CI/CD Pipelines** - Comprehensive automation
- **Docker Integration** - Containerized deployments
- **Version Management** - Semantic versioning with automation

## 🏗️ Architecture

### Branch Strategy (GitFlow)
```
main/master  ────────────────●────────●──────── (Production)
                            /          \
release/2.0.0 ────────●────●            \
                     /                    \
develop     ●───────●──────────●──────────●──── (Integration)
           /        /          /           \
feature/X ●────────           /             \
                             /               \
hotfix/1.1.1 ──────────────●                 ●
```

### Environment Flow
```
develop → DEV → TST → UAT → PREPROD → PROD (master)
```

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker (optional)
- Git

### Local Development
```bash
# Clone the repository
git clone https://github.com/yourusername/spring-boot-gitflow-demo.git
cd spring-boot-gitflow-demo

# Run the application
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access the application
open http://localhost:8080
```

### API Endpoints
- **Health Check**: `GET /api/health`
- **Application Info**: `GET /api/info`
- **Environment Info**: `GET /api/environment`
- **Users API**: `GET/POST/PUT/DELETE /api/users`
- **H2 Console**: `http://localhost:8080/h2-console` (dev profile)
- **API Documentation**: `http://localhost:8080/swagger-ui.html`

## 🔄 Workflow Overview

### 1. Pull Request Workflow (`pr-check.yml`)
**Triggers**: PR opened/updated to main branches
- ✅ Run tests with coverage
- 🔍 Security scanning (OWASP)
- 📊 Code quality analysis (SonarCloud)
- 🐳 Docker build verification
- 💬 PR comments with results

### 2. Development Deployment (`build-deploy-dev.yml`)
**Triggers**: PR merged to `develop`
- 🧪 Run tests
- 🐳 Build and publish Docker image
- 🚀 Deploy to DEV environment
- 🔍 Smoke tests
- 📢 Team notifications

### 3. Release Management (`start-release-hotfix.yml`)
**Triggers**: Issue created with labels `release:major`, `release:minor`, or `bug:hotfix`

**For Releases:**
1. Creates `release/X.Y.Z` branch from `develop`
2. Bumps version to `X.Y.Z-RC1`
3. Updates CHANGELOG.md
4. Creates PR to `master`

**For Hotfixes:**
1. Creates `hotfix/X.Y.Z` branch from `master`
2. Bumps version to `X.Y.Z`
3. Creates PR to `master`

### 4. Environment Promotion (`promote-environment.yml`)
**Triggers**: Manual workflow dispatch
- 🎯 Choose target environment (TST/UAT/PREPROD)
- 🏷️ Create environment-specific tags
- 🧪 Run environment-specific tests
- 🚀 Deploy to target environment
- ✅ Validation and health checks

## 🎪 How to Use the Workflows

### Starting a Release

1. **Create an Issue** using the release template:
   - Go to Issues → New Issue
   - Choose "Major Release" or "Minor Release"
   - Fill in the details
   - Submit the issue

2. **Automation Happens**:
   - Workflow creates release branch
   - Version is bumped
   - PR is created automatically
   - You get notified with next steps

3. **Complete the Release**:
   - Make final changes in the release branch
   - Review and merge the PR
   - Production deployment happens automatically

### Environment Promotions

1. **Go to Actions Tab**
2. **Select "Promote to Environment"**
3. **Choose Parameters**:
   - Target environment (TST/UAT/PREPROD)
   - Source commit (or leave empty for latest develop)
   - Whether to create tags
4. **Run Workflow**
5. **Monitor Progress** in the Actions tab

### Creating a Hotfix

1. **Create an Issue** using the hotfix template
2. **Fill Critical Issue Details**
3. **Submit Issue** - automation starts
4. **Fix the Issue** in the created hotfix branch
5. **Merge PR** when ready
6. **Production Deployment** happens immediately

## 🎨 Environment Configuration

Each environment has its own configuration:

- **DEV** (`application-dev.yml`): Debug logging, H2 console enabled
- **TST** (`application-tst.yml`): Info logging, testing optimizations
- **UAT** (`application-uat.yml`): User acceptance testing setup
- **PROD** (`application-prod.yml`): Production-ready, minimal logging

## 🐳 Docker Support

### Build Local Image
```bash
# Build image locally
mvn jib:dockerBuild -Pdocker-local

# Run container
docker run -p 8080:8080 spring-boot-gitflow-demo:latest
```

### Environment-Specific Images
Images are automatically built with environment tags:
- `ghcr.io/username/spring-boot-gitflow-demo:dev-1.0.0-20240101-abc123`
- `ghcr.io/username/spring-boot-gitflow-demo:tst-1.0.0-20240101-abc123`
- `ghcr.io/username/spring-boot-gitflow-demo:1.0.0` (production)

## 📋 Release Process Example

### Scenario: Creating Version 2.0.0

1. **Plan the Release**
   - Decide on features for v2.0.0
   - Ensure all features are in `develop`

2. **Create Release Issue**
   ```
   Title: Release version 2.0.0
   Labels: release:major
   ```

3. **Automation Creates**:
   - Branch: `release/2.0.0`
   - Version: `2.0.0-RC1`
   - PR: `release/2.0.0` → `master`

4. **Manual Steps**:
   - Review the release branch
   - Test thoroughly
   - Make final adjustments
   - Approve and merge PR

5. **Production Release**:
   - Version becomes `2.0.0`
   - Docker image published
   - Git tag created: `v2.0.0`
   - Deployed to production

## 🧪 Testing Strategy

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest.profile=integration
```

### Environment-Specific Tests
```bash
# Test with TST profile
mvn test -Dspring.profiles.active=tst
```

### Coverage Report
```bash
mvn jacoco:report
# View: target/site/jacoco/index.html
```

## 🔧 Configuration

### Required Secrets
Add these to your GitHub repository secrets:
- `SONAR_TOKEN`: SonarCloud token for code analysis
- `CODECOV_TOKEN`: Codecov token for coverage reports

### Optional Secrets
- `SLACK_WEBHOOK_URL`: For Slack notifications
- `DOCKER_REGISTRY_TOKEN`: For custom Docker registries

### Environment Setup
Create GitHub Environments:
- `development`
- `tst`
- `uat`  
- `preprod`
- `production`

## 📊 Monitoring and Observability

### Health Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
- Structured logging with different levels per environment
- JSON format for production environments
- Console output for development

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

## 📚 Learning Resources

### Understanding the Workflows
- Study `.github/workflows/` directory
- Review `.github/actions/` for reusable components
- Check `.github/ISSUE_TEMPLATE/` for templates

### Key Concepts Demonstrated
- **GitFlow**: Branch-based development workflow
- **Semantic Versioning**: `MAJOR.MINOR.PATCH` versioning
- **CI/CD**: Continuous Integration/Continuous Deployment
- **Environment Promotion**: Progressive deployment strategy
- **Infrastructure as Code**: All automation in version control

## 🐛 Troubleshooting

### Common Issues

**Workflow not triggering?**
- Check issue labels match exactly: `release:major`, `release:minor`, `bug:hotfix`
- Ensure you have proper permissions

**Build failures?**
- Check Java version (requires 21)
- Verify Maven dependencies
- Review test failures in Actions logs

**Docker issues?**
- Ensure Docker is running locally
- Check registry permissions for push operations

## 📈 Metrics and Analytics

The project includes examples for:
- Build time tracking
- Deployment frequency metrics
- Lead time measurements
- Mean time to recovery (MTTR)

## 🔮 Future Enhancements

- [ ] Integration with monitoring tools (Prometheus, Grafana)
- [ ] Database migrations with Flyway
- [ ] Load testing automation
- [ ] Security scanning with Snyk
- [ ] Multi-cloud deployment examples

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

- 📧 Create an issue for bugs or feature requests
- 💬 Discussions for questions and ideas
- 📖 Wiki for extended documentation

---

**Happy Coding!** 🎉

Made with ❤️ for learning GitHub workflows and GitFlow strategies.