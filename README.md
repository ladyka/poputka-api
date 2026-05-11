# poputka-api

Backend сервиса «Попутка»: Spring Boot, REST API, PostgreSQL, Liquibase.

## Требования

- **JDK 25** (совпадает с Gradle toolchain в `build.gradle`)
- Docker — для **Testcontainers** в тестах (PostgreSQL)

## Сборка и тесты

```bash
./gradlew test
```

Покрытие (JaCoCo): после тестов отчёт в `build/reports/jacoco/test/html/index.html`. Порог и исключения пакетов — в `build.gradle` (`jacocoTestCoverageVerification`, `jacocoExcludes`).

## Конфигурация

Базовые параметры: `src/main/resources/application.yaml`. Секреты и внешние сервисы — через переменные окружения (см. плейсхолдеры в yaml).

**Аутентификация:** stateless JWT, опционально Google и Apple — полный контракт в **`docs/features/auth.md`**.

## Документация

- **ИИ / разработка:** **`AGENTS.md`**
- **Фичи API:** **`docs/features/README.md`**
- **OpenAPI снимок:** `./gradlew generateOpenApiJson` → `docs/openapi/openapi.json` (см. **`docs/openapi/README.md`**)

## Правила проекта

Часть соглашений вынесена в **`.cursor/rules/`** (тестовый Liquibase, стиль кода).
