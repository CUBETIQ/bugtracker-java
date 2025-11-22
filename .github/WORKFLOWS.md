# GitHub Actions Workflows Documentation

This document describes the automated CI/CD workflows for the BugTracker SDK.

## Workflows Overview

### 1. CI - Tests & Build (ci.yml)

**Trigger**: Push to `main`/`develop` branches or Pull Requests

**Purpose**: Continuous integration - runs tests and builds the project

**Features**:
- Tests on multiple Java versions (11, 17, 21)
- Test result artifacts upload
- Build artifacts generation
- Code coverage collection (Codecov)

**Jobs**:
- `test-and-build`: Runs tests and builds on JDK 11, 17, 21
- `coverage`: Generates coverage reports

**Artifacts**:
- Test reports in `build/reports/tests/`
- JAR files in `build/libs/`

---

### 2. Quality Checks - PR Validation (pr-validation.yml)

**Trigger**: Pull Request to `main`/`develop`

**Purpose**: Validates PR quality before merge

**Features**:
- Full build and test validation
- Automatic comment on PR with status
- Test result reporting

**Checks**:
- Gradle wrapper validation
- Unit tests execution
- Full project build
- Status comment on PR

---

### 3. Release - Build & Publish JAR (release.yml)

**Trigger**: Git tag pushed with format `v*` (e.g., `v1.0.0`)

**Purpose**: Automated release process

**Features**:
- Builds and tests the release version
- Generates changelog from git history
- Creates GitHub Release with changelog
- Uploads JAR artifacts
- Publishes release notes

**Steps**:
1. Checkout code with full history
2. Run full test suite
3. Build project
4. Extract version from tag
5. Generate changelog from commits
6. Create GitHub Release with:
   - Version number
   - Changelog
   - Build information
   - JAR artifact
7. Upload artifacts for 30 days retention

**Release Notes Include**:
- Version number
- Commit-based changelog
- Build information
- Installation instructions
- Gradle/Maven coordinates

---

## How to Use

### Running Tests Locally

```bash
./gradlew test
```

### Building Locally

```bash
./gradlew build
```

### Creating a Release

1. **Update version** in `build.gradle` (if applicable)
2. **Update CHANGELOG.md** with release notes
3. **Commit changes**:
   ```bash
   git add CHANGELOG.md build.gradle
   git commit -m "Release v1.1.0"
   ```

4. **Create and push tag**:
   ```bash
   git tag -a v1.1.0 -m "Release version 1.1.0"
   git push origin v1.1.0
   ```

5. **Monitor release workflow**: 
   - Go to Actions tab
   - Watch the "Release - Build & Publish JAR" workflow
   - Once complete, release appears in Releases tab

### Accessing Build Artifacts

**During CI Run**:
- Go to Actions → CI workflow run
- Scroll to "Artifacts" section
- Download test results or build artifacts

**After Release**:
- Go to Releases page
- Download JAR from release assets

---

## Workflow Variables

### Environment
- `GITHUB_TOKEN`: Automatically provided, used for releasing
- `JAVA_VERSION`: Configurable per job (default: 17)
- `GRADLE_VERSION`: Managed by wrapper

### Artifacts Retention
- **Test results**: 90 days (default)
- **Build artifacts**: 90 days (default)  
- **Release artifacts**: 30 days

---

## Troubleshooting

### Workflow Failures

**Test failures**:
- Check "Artifacts" section for test reports
- Review job logs in Actions tab
- Ensure DSN is correctly configured

**Build failures**:
- Check Gradle build output in logs
- Verify dependencies are accessible
- Check for Java version compatibility

**Release failures**:
- Ensure tag format is `v*` (e.g., `v1.0.0`)
- Verify `GITHUB_TOKEN` has proper permissions
- Check repository is public or token has full access

### Common Issues

**"Workflow does not exist"**
- Push workflows to `.github/workflows/` directory
- Use `.yml` or `.yaml` extension

**"Test artifacts not found"**
- Tests must pass to generate artifacts
- Check "Upload test results" step in logs

**"Release not created"**
- Verify tag is pushed: `git push origin v1.0.0`
- Check Actions tab for workflow execution
- Verify `GITHUB_TOKEN` permissions

---

## Customization

### Changing Java Versions

Edit `ci.yml`:
```yaml
strategy:
  matrix:
    java-version: [ '11', '17', '21' ]  # Modify here
```

### Changing Branches

Edit workflows (e.g., `ci.yml`):
```yaml
on:
  push:
    branches: [ main, develop ]  # Modify here
```

### Adding More Tests

No workflow changes needed - `./gradlew test` runs all tests automatically

### Customizing Release Notes

Edit `release.yml` body section to add more details

---

## Best Practices

1. **Always run tests locally before pushing**:
   ```bash
   ./gradlew test
   ```

2. **Use semantic versioning** for tags:
   - `v1.0.0` - Major release
   - `v1.1.0` - Minor feature
   - `v1.0.1` - Patch/bugfix

3. **Keep CHANGELOG.md updated** with each release

4. **Review test results** before merging PRs

5. **Use meaningful commit messages** for better changelogs

6. **Tag releases with annotated tags** (not lightweight):
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   ```

---

## Examples

### Example Release Workflow

```bash
# 1. Make changes and commit
git add .
git commit -m "Add new feature X"

# 2. Update version and changelog
# Edit build.gradle, CHANGELOG.md
git add build.gradle CHANGELOG.md
git commit -m "Bump version to 1.1.0"

# 3. Create release tag
git tag -a v1.1.0 -m "Release version 1.1.0: Add feature X"

# 4. Push to trigger workflow
git push origin main
git push origin v1.1.0

# 5. Monitor in GitHub Actions
# GitHub Actions automatically:
# - Runs tests
# - Builds JAR
# - Creates release
# - Generates changelog
# - Uploads artifacts
```

### Example Pull Request Workflow

```bash
# 1. Create feature branch
git checkout -b feature/my-feature

# 2. Make changes and test locally
./gradlew test

# 3. Push and create PR
git push origin feature/my-feature

# 4. GitHub automatically:
# - Runs CI tests
# - Validates build
# - Comments status on PR

# 5. After approval, merge to main
```

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Documentation](https://docs.gradle.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [Semantic Versioning](https://semver.org/)

---

## Support

For issues with workflows:
1. Check Actions tab logs
2. Review workflow YAML syntax
3. Verify all referenced actions exist
4. Consult GitHub Actions documentation

For SDK issues:
- Open an issue on GitHub
- Check existing issues first
- Include error logs and Java version
