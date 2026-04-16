# Cline Rules for poputka-api

## Modules versions
- Java 21
- Spring Boot 3.5.11

## General Guidelines
- Follow existing code style and patterns in the project
- Maintain package structure: `by.ladyka.poputka` and subpackages
- Use Lombok annotations to reduce boilerplate code
- Use MapStruct for DTO-entity mapping

## Testing
- Write tests for new functionality in `src/test/java/`
- Follow existing test patterns (Spring Boot test with Testcontainers)
- Use `@SpringBootTest` for integration tests
- Place tests in corresponding package structure under `src/test/java/`
- Test profile is active automatically via `spring.profiles.active=test` in test task

## Database Migrations
- Use Liquibase for all database schema changes
- Place changelog files in `src/main/resources/db/changelog/`
- Name changelog files with date prefix: `YYYY-MM-DD-description.yaml`
- Master changelog: `db.changelog-master.yaml`
- **IMPORTANT**: In test Liquibase changelogs (under `src/test/resources/db/changelog/`), only insert/update/delete data in the `users` table. Create test data for other entities in Java test code via repositories/services.

## Build & Dependencies
- Gradle build system
- Add new dependencies in the appropriate configuration (implementation, testImplementation, etc.)
- Keep dependencies up to date with compatible versions
- Use version variables (ext block) for third-party libraries

## API Design
- Follow RESTful principles
- Use DTOs for request/response objects
- Keep controllers thin; delegate to services
- Use proper HTTP status codes
- Document APIs with SpringDoc OpenAPI (already configured)

## Security
- Spring Security is configured; follow existing patterns
- Use authentication/authorization annotations as needed
- Never hardcode credentials or secrets
- Use configuration properties for sensitive data

## Code Quality
- Write meaningful commit messages
- Keep methods small and focused
- Follow SOLID principles
- Add JavaDoc for public APIs and complex logic
- Use meaningful variable and method names

## Git Workflow
- Follow the project's Git workflow (GitLab CI/CD)
- Create feature branches for new work
- Write descriptive commit messages
- Ensure tests pass before pushing

## Coverage
- Code coverage with JaCoCo is configured
- Minimum coverage threshold: 80%
- Run `./gradlew test` to generate coverage reports
- Coverage reports available at `build/reports/jacoco/test/html/index.html`

