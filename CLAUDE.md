# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all modules (skip tests)
mvn clean install -DskipTests

# Build and run all tests
mvn clean install

# Run tests for a specific module
mvn test -pl easy-es-springboot-test

# Run a single test class
mvn test -pl easy-es-springboot-test -Dtest=AllTest

# Run a single test method
mvn test -pl easy-es-springboot-test -Dtest=AllTest#testInsert
```

Tests in `easy-es-springboot-test` and `easy-es-solon-test` require a running Elasticsearch instance configured in the respective `application.yml`.

## Module Structure

```
easy-es-parent/          # Parent POM with dependency management
easy-es-annotation/      # @IndexName, @IndexField, @IndexId, @HighLight, @Join, @Distance, etc.
easy-es-common/          # Constants, enums, exceptions, EasyEsProperties, shared utilities
easy-es-extension/       # Extension interfaces and base abstractions
easy-es-core/            # Core engine (see below)
easy-es-spring/          # Spring Framework integration (beans, AOP, context)
easy-es-boot-starter/    # Spring Boot auto-configuration entry point
easy-es-solon-plugin/    # Solon framework integration (alternative to Spring Boot)
easy-es-springboot-test/ # Integration tests with Spring Boot
easy-es-solon-test/      # Integration tests with Solon
easy-es-springboot-sample/ # Sample application
```

## Core Architecture

### Mapper Pattern
Users define mapper interfaces extending `BaseEsMapper<T>` (in `easy-es-core/kernel/`). At runtime, `EsMapperProxy` provides the implementation via JDK dynamic proxy. This mirrors the MyBatis-Plus pattern.

### Wrapper/Builder Pattern
All query/update/index conditions are built via fluent wrappers:
- `LambdaEsQueryWrapper<T>` — query conditions and field selection
- `LambdaEsUpdateWrapper<T>` — update conditions
- `LambdaEsIndexWrapper<T>` — index management
- Chain variants: `LambdaEsQueryChainWrapper`, `LambdaEsUpdateChainWrapper`, `LambdaEsIndexChainWrapper`
- Factory entry point: `EsWrappers` utility class

Wrappers use Java lambda method references (e.g., `Document::getTitle`) for type-safe field references, resolved to actual field names via `SFunction<T, R>`.

### Entity Metadata
`EntityInfo` (in `easy-es-core/biz/`) holds complete mapping metadata for an entity class — index name, routing, field mappings, join config, etc. `EntityInfoHelper` builds and caches these at startup by scanning annotations.

### Automatic Index Lifecycle Management
This is the standout feature. Two strategies in `easy-es-core/index/`:
- `AutoProcessIndexSmoothlyStrategy` — zero-downtime index updates: creates a new versioned index, migrates data, then switches the alias. Uses `S1`/`SO` index name suffixes.
- `AutoProcessIndexNotSmoothlyStrategy` — direct index operations without migration.

Strategy is selected via `easy-es.global-config.process-index-mode` in configuration.

### Query Processing Pipeline
`LambdaEsQueryWrapper` → `WrapperProcessor` → `SearchRequest.Builder` → Elasticsearch Java Client (`co.elastic.clients`). The processor handles keyword suffix inference, nested query wrapping, geo queries, aggregations, and highlighting.

### Framework Integration Points
- **Spring Boot**: `easy-es-boot-starter` provides `@EnableEasyEs` and auto-configuration via `EasyEsAutoConfiguration`. Mappers are registered as Spring beans.
- **Solon**: `easy-es-solon-plugin` provides equivalent integration for the Solon framework.
- Both integrations share the same `easy-es-core` and `easy-es-spring` logic.

## Key Annotations

| Annotation | Location | Purpose |
|---|---|---|
| `@IndexName` | Class | Maps entity to an ES index |
| `@IndexId` | Field | Marks the document ID field |
| `@IndexField` | Field | Configures field type, analyzer, etc. |
| `@HighLight` | Field | Marks fields for highlight queries |
| `@Join` | Class | Configures parent-child join relationships |
| `@Distance` | Field | Marks geo-distance fields |

## Configuration

Main config prefix: `easy-es` in `application.yml`. Key properties are in `EasyEsProperties` (`easy-es-common`). Global runtime state is held in `GlobalConfigCache`.
