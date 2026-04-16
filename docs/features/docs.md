# Документы: загрузка и отправка на проверку

Этот документ описывает текущий сценарий работы с пользовательскими документами, который реализован в
`DocumentController` (`/api/documents`).

## Статусы

- `DRAFT` — документ создан пользователем (на `PUT /api/documents/create`).
- `REVIEW` — документ отправлен на проверку (на `POST /api/documents/submit`).
- `DECLINE` — отклонён (используется в правилах `update/submit`, но отдельные админ-эндпоинты в коде не найдены).
- `APPROVE` — подтверждён (план/ожидаемая логика: админ-эндпоинты в коде не найдены).

## Эндпоинты

1. `GET /api/documents`
   - возвращает список документов текущего пользователя;
   - в ответе нет `fileUrl` (DTO формируется через `DocumentMapper`).

2. `PUT /api/documents/create`
   - body: `UserDocumentRequestCreateDto` (`type`, `description`, `expirationDate`);
   - создаёт документ со статусом `DRAFT` и генерирует `documentId`.

3. `PUT /api/documents/update`
   - body: `UserDocumentRequestUpdateDto` (обновляет `id`, `type`, `description`, `expirationDate`);
   - доступно только владельцу документа и только если текущий статус `DRAFT` или `DECLINE`.

4. `POST /api/documents/upload`
   - параметр: `documentId`
   - multipart: поле `files` (список `MultipartFile`)
   - возвращает `List<String>` с `fileUrl` для загруженных файлов.

5. `POST /api/documents/submit`
   - параметр: `documentId`
   - переводит статус документа в `REVIEW`, если текущий статус `DRAFT` или `DECLINE`.

## Примечания

- `documentId` генерируется на `create` и дальше используется в `upload` и `submit`.
- Отдельные эндпоинты администратора для переключения `APPROVE/DECLINE` в текущей кодовой базе не обнаружены (возможно, реализованы в другом месте/планируются).
