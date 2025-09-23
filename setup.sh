#!/bin/bash

# Spring Boot GitFlow Demo - Setup Script
# This script helps you set up the project for your personal GitHub repository

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "🚀 Spring Boot GitFlow Demo Setup"
echo "=================================="
echo -e "${NC}"

# Check prerequisites
echo -e "${YELLOW}📋 Checking prerequisites...${NC}"

# Check Java
if command -v java &> /dev/null && java -version 2>&1 | grep -q "21"; then
    echo -e "${GREEN}✅ Java 21 found${NC}"
else
    echo -e "${RED}❌ Java 21 required but not found${NC}"
    echo "Please install Java 21 from: https://adoptium.net/"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✅ Maven found${NC}"
else
    echo -e "${RED}❌ Maven required but not found${NC}"
    echo "Please install Maven from: https://maven.apache.org/install.html"
    exit 1
fi

# Check Git
if command -v git &> /dev/null; then
    echo -e "${GREEN}✅ Git found${NC}"
else
    echo -e "${RED}❌ Git required but not found${NC}"
    echo "Please install Git from: https://git-scm.com/"
    exit 1
fi

# Check Docker (optional)
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✅ Docker found (optional)${NC}"
else
    echo -e "${YELLOW}⚠️  Docker not found (optional but recommended)${NC}"
fi

echo ""

# Initialize Git repository if not already done
if [ ! -d ".git" ]; then
    echo -e "${YELLOW}🌱 Initializing Git repository...${NC}"
    git init
    git add .
    git commit -m "Initial commit: Spring Boot GitFlow Demo setup"
    echo -e "${GREEN}✅ Git repository initialized${NC}"
else
    echo -e "${GREEN}✅ Git repository already exists${NC}"
fi

# Create develop branch if it doesn't exist
if ! git branch --list | grep -q "develop"; then
    echo -e "${YELLOW}🌿 Creating develop branch...${NC}"
    git checkout -b develop
    echo -e "${GREEN}✅ Develop branch created${NC}"
else
    echo -e "${GREEN}✅ Develop branch already exists${NC}"
fi

# Test the application
echo -e "${YELLOW}🧪 Testing the application...${NC}"
if mvn clean test -q; then
    echo -e "${GREEN}✅ All tests passed${NC}"
else
    echo -e "${RED}❌ Some tests failed${NC}"
    echo "Please check the test output above"
    exit 1
fi

# Test Docker build (if Docker is available)
if command -v docker &> /dev/null; then
    echo -e "${YELLOW}🐳 Testing Docker build...${NC}"
    if mvn jib:dockerBuild -Pdocker-local -DskipTests -q; then
        echo -e "${GREEN}✅ Docker image built successfully${NC}"
    else
        echo -e "${YELLOW}⚠️  Docker build failed (non-critical)${NC}"
    fi
fi

echo ""
echo -e "${GREEN}🎉 Setup completed successfully!${NC}"
echo ""
echo -e "${BLUE}📋 Next Steps:${NC}"
echo "1. Create a new repository on GitHub"
echo "2. Add your GitHub repository as remote:"
echo -e "   ${YELLOW}git remote add origin https://github.com/yourusername/spring-boot-gitflow-demo.git${NC}"
echo "3. Push the code:"
echo -e "   ${YELLOW}git push -u origin main${NC}"
echo -e "   ${YELLOW}git push -u origin develop${NC}"
echo "4. Set up GitHub environments in your repository settings:"
echo "   - development"
echo "   - tst"  
echo "   - uat"
echo "   - preprod"
echo "   - production"
echo "5. Add required secrets (optional):"
echo "   - SONAR_TOKEN (for SonarCloud)"
echo "   - CODECOV_TOKEN (for code coverage)"
echo ""
echo -e "${BLUE}🎯 How to test the workflows:${NC}"
echo "1. Create a feature branch and make changes"
echo "2. Open a Pull Request to develop"
echo "3. Create an issue with 'release:minor' label"
echo "4. Use 'Actions' tab to promote to environments"
echo ""
echo -e "${BLUE}📚 Learn more:${NC}"
echo "- Read the README.md for detailed instructions"
echo "- Check .github/workflows/ for workflow examples"
echo "- Explore .github/ISSUE_TEMPLATE/ for issue templates"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"