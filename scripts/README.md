# BugTracker SDK Scripts

This directory contains utility scripts for building and releasing the BugTracker SDK.

## Available Scripts

### 1. release.sh - Automated Release Management

Interactive release script with version management, changelog generation, and git tag creation.

#### Features:
- ✅ Interactive version selection (major/minor/patch)
- ✅ Auto-extracts current version from `build.gradle`
- ✅ Supports custom version input
- ✅ Validates version format (X.Y.Z)
- ✅ Automatic changelog generation from git commits
- ✅ Git tag creation and push
- ✅ Dry-run mode for testing
- ✅ Full build validation before release
- ✅ Automatic GitHub Actions trigger

#### Usage:

**Interactive Mode (Recommended):**
```bash
./scripts/release.sh
```

**Specific Version:**
```bash
./scripts/release.sh -v 1.2.0
```

**Custom Tag:**
```bash
./scripts/release.sh -v 1.2.0 -t v1.2.0-release
```

**Dry Run (Preview without changes):**
```bash
./scripts/release.sh --dry-run
```

**Skip Confirmations:**
```bash
./scripts/release.sh -v 1.2.0 --force
```

#### Version Format:
- Standard: `X.Y.Z` (e.g., 1.2.0)
- Git Tag: `vX.Y.Z` (e.g., v1.2.0)

#### Release Workflow:
1. Validates git repository is clean
2. Shows version selection options
3. Confirms release details
4. Runs full build and tests
5. Updates `build.gradle` version
6. Creates git tag with changelog
7. Pushes tag to remote (triggers CI/CD)
8. GitHub Actions automatically creates release

#### Help:
```bash
./scripts/release.sh --help
```

---

### 2. build.sh - Build Convenience Script

Provides convenient build commands with various options.

#### Features:
- ✅ Clean build
- ✅ Shadow JAR creation (all dependencies bundled)
- ✅ Test-only execution
- ✅ Verbose output
- ✅ Cache cleaning

#### Usage:

**Standard Build:**
```bash
./scripts/build.sh
```

**Build with Shadow JAR:**
```bash
./scripts/build.sh --shadow
```

Output:
- Standard JAR: `build/libs/bugtracker-1.0.0.jar`
- Shadow JAR: `build/libs/bugtracker-1.0.0-all.jar`

**Run Tests Only:**
```bash
./scripts/build.sh --test-only
```

**Verbose Build Output:**
```bash
./scripts/build.sh --info
```

**Clean Cache and Rebuild:**
```bash
./scripts/build.sh --clean-cache
```

#### Combined Usage:
```bash
./scripts/build.sh --clean-cache --info --shadow
```

#### Help:
```bash
./scripts/build.sh --help
```

---

## Complete Release Example

### Step 1: Interactive Release
```bash
$ ./scripts/release.sh

===================================================
  BugTracker SDK Release Script
===================================================

ℹ Current version: 1.0.0

===================================================
  Interactive Release Version Selection
===================================================

ℹ Current version: 1.0.0

Select version to release:

  1) 2.0.0      - Major release (breaking changes)
  2) 1.1.0      - Minor release (new features)
  3) 1.0.1      - Patch release (bugfixes)
  4) Custom version   - Enter custom version
  5) Cancel           - Exit without releasing

Enter choice (1-5): 2
✓ Selected: 1.1.0

Release Summary:
  Version:     1.1.0
  Tag:         v1.1.0
  Build:       bugtracker-1.1.0-all.jar

Continue with release? (yes/no): yes
```

### Step 2: Automatic Process
- Builds and tests the project
- Creates git tag with changelog
- Pushes to remote repository
- GitHub Actions automatically:
  - Runs tests
  - Builds JAR and shadow JAR
  - Creates GitHub Release
  - Uploads artifacts

### Step 3: Access Release
- GitHub Releases page with changelog
- Download JAR and shadow JAR
- Automated release notes

---

## Output Artifacts

### Standard Build (./scripts/build.sh)
```
build/libs/
├── bugtracker-1.0.0.jar           # Standard JAR (30KB)
└── (dependencies added via Gradle)
```

### With Shadow JAR (./scripts/build.sh --shadow)
```
build/libs/
├── bugtracker-1.0.0.jar           # Standard JAR (30KB)
└── bugtracker-1.0.0-all.jar       # Shadow JAR with deps (5.3MB)
```

### Released Artifacts
Both JARs are published in GitHub Releases with full changelog and build information.

---

## Requirements

### System Requirements:
- Bash shell (macOS, Linux)
- Git 2.0+
- Java 11+
- Gradle (provided via wrapper)

### For Release:
- Clean working directory (no uncommitted changes)
- Git remote configured (origin)
- Write access to repository

---

## Troubleshooting

### Issue: "Working directory not clean"
**Solution:** Commit or stash all changes
```bash
git add .
git commit -m "Your changes"
# or
git stash
```

### Issue: "Not a git repository"
**Solution:** Ensure you're in the project root directory
```bash
cd ~/projects/bugtracker-java
```

### Issue: "Build failed"
**Solution:** Fix build errors locally first
```bash
./gradlew build
# or
./scripts/build.sh --info
```

### Issue: "Tag already exists"
**Solution:** Use different version or delete existing tag
```bash
git tag -d v1.0.0
./scripts/release.sh -v 1.0.1
```

### Issue: Script not executable
**Solution:** Make script executable
```bash
chmod +x scripts/release.sh scripts/build.sh
```

---

## Docker Usage

If you want to use scripts in a Docker container:

```dockerfile
FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y git

WORKDIR /app
COPY . .

RUN chmod +x scripts/*.sh

CMD ["./scripts/release.sh"]
```

---

## CI/CD Integration

These scripts are designed to work with GitHub Actions workflows:

- **CI Workflow**: Runs on every push (`.github/workflows/ci.yml`)
- **Release Workflow**: Triggered by version tags (`.github/workflows/release.yml`)
- **PR Validation**: Checks pull requests (`.github/workflows/pr-validation.yml`)

When you run `./scripts/release.sh`:
1. Creates git tag
2. Pushes tag to remote
3. GitHub Actions automatically detects tag
4. Release workflow runs automatically
5. GitHub Release created with changelog and artifacts

---

## Best Practices

1. **Always test locally first:**
   ```bash
   ./scripts/build.sh --test-only
   ```

2. **Preview before release (dry-run):**
   ```bash
   ./scripts/release.sh --dry-run
   ```

3. **Use semantic versioning:**
   - `1.0.0` - Major release
   - `1.1.0` - Minor feature
   - `1.0.1` - Patch/bugfix

4. **Keep CHANGELOG.md updated:**
   - Document new features
   - Document breaking changes
   - Link to issues/PRs

5. **Review changelog before release:**
   - Verify git history is accurate
   - Ensure meaningful commit messages
   - Check for squash commits if needed

---

## Advanced Usage

### Custom Release Process:

```bash
# 1. Build and test locally
./scripts/build.sh --shadow

# 2. Review changes
git diff

# 3. Create release branch
git checkout -b release/1.2.0

# 4. Update documentation
# Edit README.md, CHANGELOG.md, etc.

# 5. Commit changes
git add .
git commit -m "Release 1.2.0: Add new features"

# 6. Create pull request, get approval

# 7. Merge and release
git checkout main
git merge release/1.2.0

# 8. Use script to create tag
./scripts/release.sh -v 1.2.0
```

### Batch Release (Multiple Projects):

```bash
#!/bin/bash
# release-batch.sh

for project in bugtracker-java bugtracker-js; do
    cd ~"/projects/$project"
    ./scripts/release.sh -v 1.2.0 --force
done
```

---

## Additional Resources

- [CHANGELOG.md](../CHANGELOG.md) - Version history
- [README.md](../README.md) - Main documentation
- [.github/WORKFLOWS.md](../.github/WORKFLOWS.md) - CI/CD workflows
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)

---

## License

These scripts are part of BugTracker SDK and follow the same license as the project.

For more information, see the main project README.
