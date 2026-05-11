# Agent Rules for poputka-api

Используйте этот файл как **AGENTS.md** (рекомендуемое имя для Cursor и других ИИ-агентов).

## Stack (актуально по `build.gradle`)

- **Java 25** (toolchain)
- **Spring Boot 4.0.x** (см. `plugins { id 'org.springframework.boot' ... }`)
- Gradle, Liquibase, JPA, Spring Security, SpringDoc OpenAPI

## General Guidelines

- Соблюдать стиль и паттерны проекта; пакет `by.ladyka.poputka` и подпакеты.
- Lombok для boilerplate; **MapStruct** для маппинга DTO ↔ entity где уместно.
- Следовать правилам в `.cursor/rules/` (например, тестовый Liquibase только для `users`, без `private static void` в хелперах).

## Аутентификация и безопасность

- **Stateless JWT**: защищённые API — заголовок `Authorization: Bearer <accessToken>`. Сессии и form login отключены.
- Публичные auth-маршруты: `/api/auth/login`, `/api/auth/google`, `/api/auth/apple`, `/api/auth/refresh`, `/api/auth/logout`; link/unlink — с Bearer.
- Секреты и OAuth: только через конфиг / env (`JWT_SECRET`, `GOOGLE_CLIENT_ID`, `APPLE_AUDIENCE` и т.д.), не хардкодить.
- Подробный контракт: **`docs/features/auth.md`**.

## Testing

- Новые сценарии — интеграционные тесты в `src/test/java/`, профиль **`test`** задаётся в Gradle (`spring.profiles.active=test`).
- `@SpringBootTest` + `MockMvc` по образцу существующих контроллерных тестов.
- `@WithUserDetails(...)` по-прежнему подходит для имитации залогиненного пользователя.
- Для `POST /api/auth/google` и `/api/auth/apple` в тестах верификаторы токенов мокаются через `@TestConfiguration` + `@Primary` (см. `AuthControllerIntegrationTest`), чтобы не ходить в Google/Apple.

## Database Migrations

- Схема только через **Liquibase** в `src/main/resources/db/changelog/`.
- Имена файлов: префикс даты `YYYY-MM-DD-description.yaml`; мастер-файл: `db.changelog-master.yaml`.
- **Тестовые** changelog в `src/test/resources/db/changelog/`: изменения данных **только для таблицы `users`**; остальные сущности — из Java через репозитории/сервисы.

## Build & Dependencies

- Зависимости в `build.gradle` с правильной конфигурацией (`implementation`, `testImplementation`, …).
- Версии сторонних библиотек по возможности выносить в блок `ext { }`.

## API Design

- REST, DTO для входа/выхода, тонкие контроллеры.
- Корректные HTTP-коды; публичные эндпоинты явно в `WebSecurityConfig`.
- Документация API: SpringDoc (`/v3/api-docs`, Swagger UI). Снимок JSON: **`./gradlew generateOpenApiJson`** → `docs/openapi/openapi.json` (см. `docs/openapi/README.md`).

## Code Quality

- Осмысленные коммиты; мелкие сфокусированные изменения.
- Публичные API и нетривиальная логика — при необходимости JavaDoc.

## Coverage (JaCoCo)

- Порог **`jacocoTestCoverageVerification`**: см. `build.gradle` (сейчас **instruction covered ratio** задаётся там же).
- Из отчёта и порога исключены пакеты road assistance (см. **`jacocoExcludes`** в `build.gradle`) — при добавлении новых исключений дублировать список в **обоих** тасках: `jacocoTestReport` и `jacocoTestCoverageVerification`.
- Отчёт: `build/reports/jacoco/test/html/index.html` после `./gradlew test`.

## Документация фич

- Оглавление: **`docs/features/README.md`**.
- Описания по областям: `docs/features/*.md` (в т.ч. **`auth.md`** для входа, refresh, Google/Apple, полей `UserInfoDto`).
- При добавлении крупной фичи — обновить или добавить соответствующий файл в `docs/features/` и пункт в README.

## Git / CI

- Ветки под задачи, тесты зелёные перед пушем; следовать принятому в команде GitLab CI/CD процессу.
