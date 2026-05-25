# Weather Project

Java application.

## Tech Stack
- **Language**: Java 17+
- **Build Tool**: Maven
- **Test Framework**: JUnit 5 + Mockito

## Build & Test
```bash
mvn compile          # Compile
mvn test             # Run tests
mvn verify           # Full verification (tests + checks)
mvn package          # Package artifact
```

## Project Structure
```
src/
├── main/java/com/weather/   # Application source
└── test/java/com/weather/   # Tests
```

## Conventions
- Minimum 80% test coverage
- Follow TDD: write tests first, then implement
- Use JUnit 5 (`@Test`, `@BeforeEach`, `@DisplayName`)
- Mock external dependencies with Mockito
- No hardcoded secrets — use environment variables or config files
