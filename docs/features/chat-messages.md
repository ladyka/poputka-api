# Чат бронирования: сообщения (`message`)

Чат для бронирования хранится в таблице `message` (см. Liquibase `2025-06-10-message`).

## Модель данных

Единственное поле с данными сообщения: **`payload` (jsonb)**.

### Формат `payload`

Все сообщения — JSON-объект с полем `type`.

#### Текст пользователя

```json
{
  "type": "MESSAGE",
  "text": "Привет!"
}
```

#### Сервисное сообщение (например, смена статуса бронирования)

```json
{
  "type": "SERVICE",
  "event": "BOOKING_STATUS_CHANGED",
  "from": "WAITING",
  "to": "ACCEPTED"
}
```

Примечания:
- `booking_id` уже есть в строке `message` и **не дублируется** в `payload`.
- `actor` и время фиксируются через существующие поля `sender_id` и audit-поля (`created_*` / `created_datetime`).

#### Расширяемые типы (план)

Дальше можно добавлять payload-типы без изменения схемы таблицы, например:
- `LOCATION` — координаты/точка/подпись
- `VOICE` / `PHOTO` / `FILE` — ссылки на файлы + метаданные

## API (план)

### Получение сообщений

Сейчас клиент получает “текст сообщения” отдельным полем. После миграции:

- клиент получает **JSON**, который является десериализацией `payload`
- для обратной совместимости на переходный период можно (опционально) дублировать `payload` в поле ответа `payload` и **не** отдавать `content`

### Смена статуса бронирования из чата

План:
- `POST /api/booking/{bookingId}/status` с телом `{ "to": "ACCEPTED" }`
- сервер валидирует переход и права
- создаёт `message` с `payload.type = "SERVICE"` и `BOOKING_STATUS_CHANGED`

### UI: какие переходы доступны

План:
- `GET /api/booking/{bookingId}/available-statuses`

## Миграция данных (Liquibase)

См. `src/main/resources/db/changelog/2026-04-17-message-payload-migration.yaml`.

Миграция подключена в `db.changelog-master.yaml` и переносит исторические `content` в `payload`.

Идея:
1. Добавить `payload jsonb`.
2. Заполнить `payload` для всех существующих строк:
   - `payload = jsonb_build_object('type','MESSAGE','text', to_jsonb(content))`
3. Сделать `payload` NOT NULL.
4. Удалить колонку `content`.
