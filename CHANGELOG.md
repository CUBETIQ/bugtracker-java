# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- GitHub Actions CI/CD workflows for automated testing and releases
- Multi-version Java testing (11, 17, 21)
- Changelog tracking and automatic generation in releases

### Changed
- Enhanced test suite with real exception scenarios
- Real Sentry DSN integration for testing

### Fixed
- Test assertions for event builder tags count
- Event builder tag count expectations

---

## [1.0.0] - 2025-11-22

### Added
- Initial release of BugTracker SDK for Java
- Complete Sentry wrapper with fluent APIs
- **Core Managers**:
  - `BreadcrumbManager` for event tracking
  - `ContextManager` for user and metadata tracking
  - `HookManager` for event transformation
  - `TransactionManager` for performance monitoring
- **Builders**:
  - `BugTrackerConfig` for configuration management
  - `EventBuilder` for custom event creation
  - `BreadcrumbBuilder` for breadcrumb creation
- **Integrations**:
  - Java Util Logging handler integration
  - Sentry 8.26.0 full integration
- **Logging**:
  - SLF4J and Logback support
  - Comprehensive logging throughout SDK
- **Testing**:
  - 22 comprehensive unit tests
  - Multi-scenario exception handling tests
  - Real Sentry endpoint integration
- **Documentation**:
  - README.md with complete feature guide
  - GETTING_STARTED.md with quick-start examples
  - ARCHITECTURE.md with system design documentation
  - 8 production-ready code examples

### Features
- Flexible configuration with multiple backends
- High-performance asynchronous event transmission
- Thread-safe operations with isolated scopes
- Event lifecycle hooks for transformation
- Breadcrumb categorization (HTTP, database, user actions)
- User context and custom tag management
- Performance transaction tracking with spans
- Graceful error handling and defaults
- Developer-friendly fluent APIs
- 100% test coverage for core functionality

### Dependencies
- Sentry SDK 8.26.0
- SLF4J 2.0.11
- Logback 1.4.14
- Google Guava 32.1.3-jre
- JUnit 5.10.0 (testing)
- Mockito 5.7.0 (testing)

### Known Limitations
- Java 11+ required (not compatible with Java 8)
- Requires active Sentry project for full functionality
- Currently supports Gradle as build tool

### Future Enhancements
- [ ] Spring Boot auto-configuration starter
- [ ] SLF4J appender implementation
- [ ] Micrometer metrics integration
- [ ] OpenTelemetry compatibility
- [ ] Maven Pom XML support
- [ ] Additional framework examples (Spring, Quarkus, Vert.x)

---

## Release Notes

### Version 1.0.0
- Production-ready BugTracker SDK
- Full Sentry integration
- Comprehensive test suite
- Complete documentation

### Installation
```gradle
implementation 'com.cubetiqs.sdk:bugtracker:1.0.0'
```

### Quick Start
```java
BugTrackerClient tracker = BugTrackerClient.builder()
    .setDsn("https://8fac51b682544aa8becdc8c364d812e1@bugtracker.ctdn.dev/7")
    .setEnvironment("production")
    .build();

tracker.initialize();
tracker.captureException(new Exception("Something went wrong"));
tracker.close();
```

---

## How to Update

To upgrade to the latest version, update your dependency:

```gradle
// Old version
implementation 'com.cubetiqs.sdk:bugtracker:1.0.0'

// New version
implementation 'com.cubetiqs.sdk:bugtracker:1.1.0'
```

---

## Contributing

When contributing, please update the [Unreleased] section with your changes following the Keep a Changelog format.

---

For more information, visit the [GitHub Repository](https://github.com/cubetiq/bugtracker-java)
