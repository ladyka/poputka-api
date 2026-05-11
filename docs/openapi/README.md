# OpenAPI (SpringDoc)

Живое описание API: **`GET /v3/api-docs`** (JSON) при запущенном приложении; UI: **`/swagger-ui.html`**.

## Снимок `openapi.json` в репозитории

Сгенерировать файл **`docs/openapi/openapi.json`** (для фронта, диффов контракта, CI):

```bash
./gradlew generateOpenApiJson
```

Таск запускает только тест с тегом **`openapi-doc`**: `OpenApiJsonGenerationTest`.

Путь вывода можно переопределить:

```bash
./gradlew generateOpenApiJson -Dopenapi.output.file=/tmp/openapi.json
```

После изменений контроллеров / DTO / security имеет смысл перегенерировать снимок и закоммитить при необходимости.
