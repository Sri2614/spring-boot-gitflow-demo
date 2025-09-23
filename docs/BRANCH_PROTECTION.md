# Branch Protection Strategy

This document outlines the branch protection rules and policies implemented for our GitFlow workflow.

## Protected Branches

### Main Branch (`main`)
**Purpose**: Production-ready code only

**Protection Rules**:
- ✅ **Require pull request reviews before merging** (1 required reviewer)
- ✅ **Dismiss stale PR approvals when new commits are pushed**
- ✅ **Require status checks to pass before merging**
  - Required check: "Build project with Java 21 and ensure quality gates"
- ✅ **Require branches to be up to date before merging**
- ✅ **Restrict pushes that create files larger than 100MB**
- ✅ **Enforce all configured restrictions for administrators**
- ❌ **Allow force pushes** - DISABLED for safety
- ❌ **Allow deletions** - DISABLED for safety

### Develop Branch (`develop`)
**Purpose**: Integration branch for feature development

**Protection Rules**:
- ✅ **Require pull request reviews before merging** (1 required reviewer)
- ✅ **Dismiss stale PR approvals when new commits are pushed**
- ✅ **Require status checks to pass before merging**
  - Required check: "Build project with Java 21 and ensure quality gates"
- ✅ **Require branches to be up to date before merging**
- ❌ **Enforce restrictions for administrators** - Disabled for flexibility
- ❌ **Allow force pushes** - DISABLED for safety
- ❌ **Allow deletions** - DISABLED for safety

## Branch Workflow Rules

### Feature Branches (`feature/*`)
- **Source**: `develop` branch
- **Target**: `develop` branch via Pull Request
- **Requirements**:
  - Must pass all CI/CD checks
  - Require 1 code review approval
  - Must be up-to-date with target branch

### Release Branches (`release/*`)
- **Source**: `develop` branch  
- **Target**: `main` branch via Pull Request
- **Requirements**:
  - Must pass all CI/CD checks
  - Require 1 code review approval
  - Version bump required
  - CHANGELOG.md must be updated

### Hotfix Branches (`hotfix/*`)
- **Source**: `main` branch
- **Target**: `main` branch via Pull Request
- **Requirements**:
  - Must pass all CI/CD checks
  - Require 1 code review approval
  - Critical fixes only
  - Must be merged back to `develop`

## Required Status Checks

All protected branches require the following status check to pass:

### "Build project with Java 21 and ensure quality gates"
This check ensures:
- ✅ **Code Compilation**: All Java code compiles successfully
- ✅ **Unit Tests**: All unit tests pass
- ✅ **Integration Tests**: Integration tests are successful
- ✅ **Code Coverage**: Minimum code coverage thresholds met
- ✅ **Security Scanning**: OWASP dependency check passes
- ✅ **Code Quality**: SonarCloud quality gates pass (optional)
- ✅ **Docker Build**: Container image builds successfully

## Code Review Requirements

### Automatic Reviewer Assignment
- **CODEOWNERS** file defines automatic reviewer assignment
- All changes require review from `@Sri2614` (repository owner)
- Specific file types have specialized review requirements:
  - Java files: Backend developer review
  - CI/CD files: DevOps review  
  - Configuration files: DevOps review
  - Security files: Security review

### Review Policies
- **1 Approving Review Required**: Minimum one approval needed
- **Dismiss Stale Reviews**: New commits invalidate old approvals
- **Code Owner Reviews**: Required for sensitive file changes
- **Conversation Resolution**: All review comments must be resolved

## Enforcement Levels

### Administrator Enforcement
- **Main Branch**: Administrators must follow all rules
- **Develop Branch**: Administrators have flexibility for urgent fixes

### Emergency Procedures
In case of critical production issues:
1. Create hotfix branch from `main`
2. Implement minimal fix with tests
3. Create emergency PR with detailed justification
4. Expedited review process (same day approval)
5. Deploy immediately after merge
6. Post-incident review and documentation

## Branch Naming Conventions

- `feature/[ticket-id]-[short-description]`
- `release/[version-number]`  
- `hotfix/[version-number]`
- `bugfix/[ticket-id]-[short-description]`

## Monitoring and Compliance

### Metrics Tracked
- PR merge time (target: < 2 business days)
- Code review participation
- Failed status check frequency
- Hotfix frequency and root cause

### Regular Reviews
- Monthly branch protection effectiveness review
- Quarterly GitFlow process retrospective
- Annual security and compliance audit

---

**Last Updated**: 2025-01-23  
**Next Review**: 2025-04-23  
**Owner**: DevOps Team (@Sri2614)