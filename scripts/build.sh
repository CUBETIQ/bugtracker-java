#!/bin/bash

###############################################################################
# BugTracker SDK Build Script
# 
# This script provides convenient build commands with options
#
# Usage:
#   ./scripts/build.sh                    # Clean build
#   ./scripts/build.sh --shadow           # Build with shadow JAR
#   ./scripts/build.sh --test-only        # Run tests only
#   ./scripts/build.sh --info             # Verbose build output
#   ./scripts/build.sh --clean-cache      # Clean all caches
###############################################################################

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}===================================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

show_help() {
    cat << EOF
${CYAN}BugTracker SDK Build Script${NC}

${BLUE}USAGE:${NC}
  ./scripts/build.sh [OPTIONS]

${BLUE}OPTIONS:${NC}
  --shadow           Build with shadow JAR (all dependencies included)
  --test-only        Run tests only (no build)
  --info             Verbose build output
  --clean-cache      Clean all caches and rebuild
  --help             Show this help message

${BLUE}EXAMPLES:${NC}
  # Standard clean build
  ./scripts/build.sh

  # Build with shadow JAR for distribution
  ./scripts/build.sh --shadow

  # Run tests only
  ./scripts/build.sh --test-only

  # Clean build with verbose output
  ./scripts/build.sh --clean-cache --info

${BLUE}OUTPUT:${NC}
  • Standard JAR:     build/libs/bugtracker-*.jar
  • Shadow JAR:       build/libs/bugtracker-*-all.jar
  • Test Reports:     build/reports/tests/

EOF
}

main() {
    cd "$PROJECT_ROOT"
    
    local gradle_cmd="./gradlew"
    local tasks="clean build"
    local extra_flags=""
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --shadow)
                print_header "Building with Shadow JAR"
                tasks="clean build shadowJar"
                shift
                ;;
            --test-only)
                print_header "Running Tests Only"
                tasks="test"
                shift
                ;;
            --info)
                extra_flags="--info"
                shift
                ;;
            --clean-cache)
                print_header "Cleaning All Caches"
                $gradle_cmd clean --no-daemon
                tasks="build"
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    print_header "BugTracker SDK Build"
    
    # Run Gradle
    $gradle_cmd $tasks --no-daemon $extra_flags
    
    # Show output
    print_success "Build completed successfully!"
    echo ""
    echo -e "${CYAN}Output artifacts:${NC}"
    
    if ls build/libs/bugtracker-*.jar 1> /dev/null 2>&1; then
        ls -lh build/libs/bugtracker-*.jar | awk '{print "  " $9 " (" $5 ")"}'
    fi
    
    echo ""
}

main "$@"
