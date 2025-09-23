---
name: Major Release
about: Create a major release (X.0.0) with breaking changes
title: 'Release version [X.0.0]'
labels: ['release:major', 'release']
assignees: ''
---

## 🚀 Major Release Request

**Target Version:** X.0.0

### 📋 Release Checklist

- [ ] All features for this release are complete
- [ ] Breaking changes are documented
- [ ] Migration guides are prepared
- [ ] All tests are passing
- [ ] Documentation is updated

### 🆕 New Features & Improvements
<!-- List new features and improvements -->

- Feature 1: Description
- Feature 2: Description
- Improvement 1: Description

### 💥 Breaking Changes
<!-- List breaking changes and their impact -->

- Breaking change 1: Description and migration path
- Breaking change 2: Description and migration path

### 📖 Documentation Updates
<!-- List documentation that needs to be updated -->

- [ ] API documentation
- [ ] User guides
- [ ] Migration guides
- [ ] README updates

### 🐛 Bug Fixes
<!-- List major bug fixes included -->

- Bug fix 1: Description
- Bug fix 2: Description

### 🔄 Dependencies
<!-- List major dependency updates -->

- Dependency 1: old version → new version
- Dependency 2: old version → new version

---

**Note:** This issue will trigger the automated release workflow when created. The workflow will:
1. Create a `release/X.0.0` branch from `develop`
2. Bump version to `X.0.0-RC1`
3. Run tests and builds
4. Create a pull request to `master`