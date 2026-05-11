# Документация по фичам (backend)

Краткий указатель файлов в этой папке. Детали контрактов сверяйте с кодом и **OpenAPI** (`/v3/api-docs` или снимок `docs/openapi/openapi.json`).

| Документ | Содержание |
|----------|------------|
| [auth.md](./auth.md) | JWT, refresh, Google / Apple Sign-In, link/unlink, cookie, env |
| [trips.md](./trips.md) | Поездки: CRUD, поиск, owned, popular, доступ к «старым» поездкам |
| [trips-booking.md](./trips-booking.md) | Бронирование, обзор водителя, статусы |
| [chat-messages.md](./chat-messages.md) | Сообщения чата брони, `payload` jsonb |
| [trips-booking-review.md](./trips-booking-review.md) | Отзывы попутчиков по брони, модерация |
| [documents.md](./documents.md) | Загрузка и отправка документов на проверку (RU) |
| [docs.md](./docs.md) | Алиас/описание сценария документов (RU) |
| [road-assistance.md](./road-assistance.md) | Взаимопомощь на дороге |

Правила для ИИ-агентов по репозиторию: корневой **`AGENTS.md`**.
