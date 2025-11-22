#!/bin/bash

###############################################################################
# BugTracker SDK Release Script
# 
# This script automates the release process for BugTracker SDK
# Features:
#   - Interactive mode for choosing version
#   - Auto-extract version from build.gradle
#   - Custom version support
#   - Changelog generation
#   - Git tag creation
#   - Build verification
#
# Usage:
#   ./scripts/release.sh              # Interactive mode (recommended)
#   ./scripts/release.sh -v 1.2.0    # Direct version
#   ./scripts/release.sh --help       # Show help
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_GRADLE="$PROJECT_ROOT/build.gradle"
CHANGELOG="$PROJECT_ROOT/CHANGELOG.md"

# Global variables
VERSION=""
TAG=""
DRY_RUN=false
FORCE=false

###############################################################################
# Functions
###############################################################################

# Print colored output
print_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}===================================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

# Show help
show_help() {
    cat << EOF
${CYAN}BugTracker SDK Release Script${NC}

${BLUE}USAGE:${NC}
  ./scripts/release.sh [OPTIONS]

${BLUE}OPTIONS:${NC}
  -v, --version VERSION    Set specific version (e.g., 1.2.0)
  -t, --tag TAG            Set specific git tag (e.g., v1.2.0)
  -d, --dry-run            Show what would be done without making changes
  -f, --force              Skip confirmations
  -h, --help               Show this help message

${BLUE}EXAMPLES:${NC}
  # Interactive mode (default - recommended)
  ./scripts/release.sh

  # Release specific version
  ./scripts/release.sh -v 1.2.0

  # Dry run to see what would happen
  ./scripts/release.sh --dry-run

  # Force release without confirmations
  ./scripts/release.sh -v 1.2.0 --force

${BLUE}FEATURES:${NC}
  • Auto-detects current version from build.gradle
  • Interactive mode for choosing version
  • Custom version/tag support
  • Changelog generation from git history
  • Git tag creation and push
  • Build verification before release
  • Dry-run mode for testing

${BLUE}REQUIREMENTS:${NC}
  • Git must be installed
  • build.gradle must exist
  • Clean working directory (no uncommitted changes)
  • Proper git configuration

${BLUE}WORKFLOW:${NC}
  1. Extracts current version from build.gradle
  2. Shows options to auto-increment or custom version
  3. Verifies repository is clean
  4. Runs full build and tests
  5. Generates changelog from git commits
  6. Creates git tag with changelog
  7. Pushes tag to remote repository
  8. GitHub Actions automatically creates release

${BLUE}TROUBLESHOOTING:${NC}
  Problem: "Not a git repository"
  Solution: Run script from within a git repository

  Problem: "Working directory not clean"
  Solution: Commit or stash all changes before releasing

  Problem: "Tag already exists"
  Solution: Use different version or delete existing tag

${CYAN}For more information, see CHANGELOG.md${NC}
EOF
}

# Extract version from build.gradle
extract_version_from_gradle() {
    if [ ! -f "$BUILD_GRADLE" ]; then
        print_error "build.gradle not found at $BUILD_GRADLE"
        exit 1
    fi
    
    local version=$(grep "^version = " "$BUILD_GRADLE" | sed "s/version = '//;s/'//")
    echo "$version"
}

# Validate version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Invalid version format: $version (expected: X.Y.Z)"
        return 1
    fi
    return 0
}

# Check git repository
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not a git repository. Please run this script from the project root."
        exit 1
    fi
}

# Check clean working directory
check_clean_working_dir() {
    if [ -n "$(git status --porcelain)" ]; then
        print_error "Working directory not clean. Please commit or stash all changes."
        print_info "Uncommitted changes:"
        git status --short | sed 's/^/  /'
        exit 1
    fi
}

# Check if tag exists
check_tag_exists() {
    local tag=$1
    if git rev-parse "$tag" >/dev/null 2>&1; then
        print_warning "Tag $tag already exists"
        return 0
    fi
    return 1
}

# Generate changelog from git history
generate_changelog() {
    local current_tag=$1
    local previous_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    
    if [ -z "$previous_tag" ]; then
        # First release - get all commits
        git log --pretty=format:"- %h: %s (%an)" | head -20
    else
        # Get commits since last tag
        git log "$previous_tag..$current_tag" --pretty=format:"- %h: %s (%an)" || echo "- Initial release"
    fi
}

# Run build and tests
run_build() {
    print_header "Building and Testing"
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would run: ./gradlew clean build"
        return 0
    fi
    
    if ! ./gradlew clean build --no-daemon; then
        print_error "Build failed. Please fix errors before releasing."
        exit 1
    fi
    
    print_success "Build and tests passed"
}

# Interactive version selection
interactive_version_select() {
    local current_version=$1
    local major=$(echo $current_version | cut -d. -f1)
    local minor=$(echo $current_version | cut -d. -f2)
    local patch=$(echo $current_version | cut -d. -f3)
    
    local next_major=$((major + 1))
    local next_minor=$((minor + 1))
    local next_patch=$((patch + 1))
    
    print_header "Interactive Release Version Selection"
    print_info "Current version: ${CYAN}$current_version${NC}"
    
    echo -e "\n${BLUE}Select version to release:${NC}\n"
    echo -e "  1) ${GREEN}$next_major.0.0${NC}      - Major release (breaking changes)"
    echo -e "  2) ${GREEN}$major.$next_minor.0${NC}  - Minor release (new features)"
    echo -e "  3) ${GREEN}$major.$minor.$next_patch${NC}  - Patch release (bugfixes)"
    echo -e "  4) ${YELLOW}Custom version${NC}   - Enter custom version"
    echo -e "  5) ${RED}Cancel${NC}           - Exit without releasing"
    echo ""
    
    read -p "Enter choice (1-5): " choice
    
    case $choice in
        1)
            VERSION="$next_major.0.0"
            print_success "Selected: $VERSION"
            ;;
        2)
            VERSION="$major.$next_minor.0"
            print_success "Selected: $VERSION"
            ;;
        3)
            VERSION="$major.$minor.$next_patch"
            print_success "Selected: $VERSION"
            ;;
        4)
            read -p "Enter custom version (X.Y.Z): " VERSION
            if ! validate_version "$VERSION"; then
                print_error "Invalid version format"
                exit 1
            fi
            print_success "Selected: $VERSION"
            ;;
        5)
            print_warning "Release cancelled"
            exit 0
            ;;
        *)
            print_error "Invalid choice"
            exit 1
            ;;
    esac
}

# Confirm release
confirm_release() {
    if [ "$FORCE" = true ]; then
        return 0
    fi
    
    echo ""
    print_info "Release Summary:"
    echo "  Version:     ${GREEN}$VERSION${NC}"
    echo "  Tag:         ${GREEN}$TAG${NC}"
    echo "  Build:       ${GREEN}bugtracker-$VERSION-all.jar${NC}"
    echo ""
    
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN MODE]${NC} No changes will be made"
        echo ""
    fi
    
    read -p "Continue with release? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        print_warning "Release cancelled"
        exit 0
    fi
}

# Create git tag
create_git_tag() {
    print_header "Creating Git Tag"
    
    if check_tag_exists "$TAG"; then
        read -p "Tag exists. Overwrite? (yes/no): " overwrite
        if [ "$overwrite" != "yes" ]; then
            print_error "Release cancelled"
            exit 1
        fi
        
        if [ "$DRY_RUN" = false ]; then
            git tag -d "$TAG" || true
        fi
    fi
    
    # Generate changelog
    local changelog_content=$(generate_changelog "$TAG")
    local tag_message="Release version $VERSION

## Changes
$changelog_content

## Build Information
- **Date**: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
- **Version**: $VERSION
- **Artifact**: bugtracker-$VERSION-all.jar

---
Released by BugTracker Release Script"
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would create tag: $TAG"
        echo -e "\n${CYAN}Tag message:${NC}"
        echo "$tag_message" | sed 's/^/  /'
        return 0
    fi
    
    # Create annotated tag
    git tag -a "$TAG" -m "$tag_message"
    print_success "Git tag created: $TAG"
}

# Push tag to remote
push_tag() {
    print_header "Pushing Tag to Remote"
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would push tag: $TAG"
        return 0
    fi
    
    # Get remote name
    local remote=$(git remote | head -n 1)
    if [ -z "$remote" ]; then
        print_error "No remote repository configured"
        exit 1
    fi
    
    if git push "$remote" "$TAG"; then
        print_success "Tag pushed to $remote"
        print_info "GitHub Actions will automatically:"
        echo "  • Run full test suite"
        echo "  • Build JAR and shadow JAR"
        echo "  • Create GitHub Release"
        echo "  • Upload artifacts"
        echo ""
        print_info "Check Actions tab: https://github.com/$(git config --get remote.origin.url | sed 's/.*://;s/.git$//')/actions"
    else
        print_error "Failed to push tag"
        exit 1
    fi
}

# Update build.gradle version
update_gradle_version() {
    print_header "Updating build.gradle Version"
    
    if [ "$DRY_RUN" = true ]; then
        print_info "[DRY RUN] Would update version to: $VERSION"
        return 0
    fi
    
    # Backup file
    cp "$BUILD_GRADLE" "$BUILD_GRADLE.bak"
    
    # Update version using sed
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/^version = .*/version = '$VERSION'/" "$BUILD_GRADLE"
    else
        # Linux
        sed -i "s/^version = .*/version = '$VERSION'/" "$BUILD_GRADLE"
    fi
    
    print_success "Updated version in build.gradle"
    
    # Commit the version update
    git add "$BUILD_GRADLE"
    git commit -m "Bump version to $VERSION" || true
}

# Show final summary
show_summary() {
    print_header "Release Summary"
    
    if [ "$DRY_RUN" = true ]; then
        print_warning "DRY RUN MODE - No changes were made"
        echo ""
    fi
    
    echo -e "${GREEN}Release prepared successfully!${NC}\n"
    
    if [ "$DRY_RUN" = false ]; then
        echo "Next steps:"
        echo "  1. Tag has been pushed to remote"
        echo "  2. GitHub Actions workflow is starting"
        echo "  3. Release will appear on GitHub Releases page"
        echo "  4. Monitor: https://github.com/$(git config --get remote.origin.url | sed 's/.*://;s/.git$//')/actions"
        echo ""
        echo -e "Release details:"
        echo -e "  Version:       ${GREEN}$VERSION${NC}"
        echo -e "  Tag:           ${GREEN}$TAG${NC}"
        echo -e "  Shadow JAR:    ${GREEN}bugtracker-$VERSION-all.jar${NC}"
        echo -e "  Standard JAR:  ${GREEN}bugtracker-$VERSION.jar${NC}"
        echo ""
    else
        echo -e "${YELLOW}To perform actual release, remove --dry-run flag${NC}"
        echo ""
    fi
}

###############################################################################
# Main Script
###############################################################################

main() {
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -v|--version)
                VERSION="$2"
                shift 2
                ;;
            -t|--tag)
                TAG="$2"
                shift 2
                ;;
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            -f|--force)
                FORCE=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    print_header "BugTracker SDK Release Script"
    
    # Verify git repo
    check_git_repo
    
    # Check clean working directory
    check_clean_working_dir
    
    # Extract current version
    local current_version=$(extract_version_from_gradle)
    print_info "Current version: ${CYAN}$current_version${NC}"
    
    # Get version if not provided
    if [ -z "$VERSION" ]; then
        interactive_version_select "$current_version"
    else
        if ! validate_version "$VERSION"; then
            print_error "Invalid version format: $VERSION"
            exit 1
        fi
        print_success "Using version: $VERSION"
    fi
    
    # Set tag if not provided
    if [ -z "$TAG" ]; then
        TAG="v$VERSION"
    fi
    
    # Confirm before proceeding
    confirm_release
    
    # Update build.gradle version
    if [ "$DRY_RUN" = false ]; then
        update_gradle_version
    fi
    
    # Run build
    run_build
    
    # Create tag
    create_git_tag
    
    # Push tag
    push_tag
    
    # Show summary
    show_summary
}

# Run main function
main "$@"
