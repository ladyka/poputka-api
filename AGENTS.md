# AGENTS.md

## Project overview

- Repository: `poputka-api`
- Stack: Java 17, Spring Boot 3.5, Spring Web/JPA/Security/Mail, Liquibase, PostgreSQL
- Build tool: Gradle 8.8 via the wrapper (`./gradlew`)
- Main application class: `src/main/java/by/ladyka/poputka/PoputkaApiApplication.java`

## Repository layout

- `build.gradle` - build, dependencies, JaCoCo coverage rules
- `settings.gradle` - Gradle project name
- `src/main/java/by/ladyka/poputka/` - application source
- `src/main/resources/application.yaml` - main runtime configuration
- `src/main/resources/db/changelog/` - production Liquibase changelogs
- `src/test/java/by/ladyka/poputka/` - tests
- `src/test/resources/application-test.yaml` - test profile configuration
- `src/test/resources/db/changelog/` - test Liquibase changelog entrypoint
- `.gitlab-ci.yml` - CI delegates to a shared GitLab pipeline

## Commands agents should use

Run all commands from the repository root and prefer the Gradle wrapper over a system Gradle install.

- `./gradlew test` - run the test suite with the `test` Spring profile
- `./gradlew check` - run verification tasks, including coverage enforcement
- `./gradlew build` - full build
- `./gradlew bootRun` - run the service locally

## Testing and verification expectations

- The `test` task forces `spring.profiles.active=test`.
- Tests use Testcontainers PostgreSQL through `jdbc:tc:postgresql:16:///test`.
- JaCoCo coverage verification is wired into `test` and `check`.
- The repository enforces a minimum instruction coverage ratio of `0.4`, so changes that reduce coverage can fail CI.

## Configuration notes

- The application listens on port `10001` by default.
- Local runtime depends on environment variables for the database, SMTP, storage, and moderator usernames.
- The main profile uses `ddl-auto: validate`; do not rely on Hibernate to create or alter schema changes.

## Liquibase rules

- Put production schema/data migrations in `src/main/resources/db/changelog/`.
- Be careful with test changelogs under `src/test/resources/db/changelog/`.
- In test Liquibase changelogs, data changes are allowed only for the `users` table.
- Do not insert or mutate non-user test data through test Liquibase migrations; create those entities in Java test code instead.

## Change guidance

- Prefer small, targeted changes that match the existing Spring Boot and Gradle conventions.
- Keep configuration changes consistent between `application.yaml` and `application-test.yaml` when both environments are affected.
- If a change touches persistence, review the relevant Liquibase changelog and the affected integration tests together.
- When adding or updating tests, prefer repository/service-driven setup for domain data instead of static database fixtures.

## CI notes

- CI is defined through a shared GitLab include, so this repository does not expose every pipeline detail locally.
- Before finishing work, at minimum run the smallest relevant Gradle verification task for the files you changed; prefer `./gradlew test` or `./gradlew check` when practical.
