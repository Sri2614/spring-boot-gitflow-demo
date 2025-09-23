---
name: Hotfix Release
about: Create a hotfix release (X.Y.Z) for critical production issues
title: 'Hotfix version [X.Y.Z]'
labels: ['bug:hotfix', 'hotfix', 'urgent']
assignees: ''
---

## 🚨 Hotfix Release Request

**Target Version:** X.Y.Z
**Severity:** Critical/High/Medium

### 🔍 Issue Description
<!-- Describe the critical issue that needs immediate fixing -->

**Problem:** Clear description of the issue

**Impact:** How this affects users/system

**Root Cause:** Brief explanation of what caused the issue

### 📋 Hotfix Checklist

- [ ] Issue reproduced and confirmed
- [ ] Root cause identified
- [ ] Fix implemented and tested
- [ ] No breaking changes introduced
- [ ] Minimal scope - only essential changes
- [ ] Tests updated/added for the fix

### 🛠️ Changes Made
<!-- List the specific changes made to fix the issue -->

- Change 1: Description
- Change 2: Description

### 🧪 Testing Performed
<!-- Describe testing done to verify the fix -->

- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Regression testing performed
- [ ] Performance impact assessed

### 📊 Risk Assessment
<!-- Assess the risk of this hotfix -->

**Risk Level:** Low/Medium/High

**Potential Impact:** Description of any potential side effects

**Rollback Plan:** How to rollback if issues arise

### 🔄 Files Changed
<!-- List the key files modified -->

- `src/main/java/com/example/demo/...`
- `src/test/java/com/example/demo/...`

### 🚀 Deployment Plan
<!-- Describe the deployment strategy -->

- [ ] Deploy to TEST environment first
- [ ] Validate fix in TEST
- [ ] Deploy to PROD with monitoring
- [ ] Monitor logs and metrics post-deployment

---

**Note:** This issue will trigger the automated hotfix workflow when created. The workflow will:
1. Create a `hotfix/X.Y.Z` branch from `master`
2. Bump version to `X.Y.Z`
3. Run tests and builds
4. Create a pull request to `master`