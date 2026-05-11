# Trips (поездки)

Документация по API поездок и правилам доступа (бекенд и фронтенд). Точные имена полей в JSON сверяйте с **OpenAPI** и ответами нужного окружения.

**Аутентификация:** stateless **JWT** — заголовок `Authorization: Bearer <accessToken>`. Вход и refresh: `docs/features/auth.md`. Защищённые маршруты без валидного токена возвращают **401** JSON (редиректа на форму логина нет).

Связанная документация:
- бронирования, обзор водителя (`/api/booking/trip/.../overview`), статусы: `docs/features/trips-booking.md`
- сообщения чата (`message`, `payload jsonb`): `docs/features/chat-messages.md`

## Эндпоинты

### Создание поездки

- **URL**: `POST /api/trip/`
- **Auth**: Bearer JWT (`Authorization: Bearer <accessToken>`)
- **Body**: `TripCreateRequestDto`
  - `startEpochMillis` — UTC epoch millis
  - `startEpochMillis` должен быть **не раньше чем через 1 час** от текущего момента
  - `passengers` — **0..100**
- **Ответ**: `TripDto`

### Обновление поездки

- **URL**: `PUT /api/trip/{id}`
- **Auth**: Bearer JWT
- **Body**: `TripUpdateRequestDto` (без `id` в body)
- **Ответ**: `TripDto`
- **Ошибки**:
  - **404 NOT_FOUND**: попытка обновить поездку, которая не принадлежит текущему пользователю

### Поиск поездок

- **URL**: `POST /api/trip/search`
- **Auth**: публичный
- **Body**: `TripSearchRequest` (`placeFrom`, `placeTo`) — **названия городов** (ищем по `trips.place_from_city`, `trips.place_to_city`)
- **Поведение**:
  - возвращаются только поездки с `start > now` (по времени старта)
  - сортировка по времени старта (возрастание)

### Мои поездки (как водитель или как пассажир)

- **URL**: `GET /api/trip/owned`
- **Auth**: Bearer JWT. Важно: этот маршрут зарегистрирован **до** общего правила «все `GET /api/trip/**` публичны» (`WebSecurityConfig`), иначе список «своих» поездок оказался бы доступен всем подряд.
- **Query**:
  - `participant` (опционально): если **не передавать** — **все релевантные** поездки: пользователь **владелец** или у него есть **бронь** как пассажир (объединение, без дублей по `trip.id`).
    - `owner`: только поездки, где пользователь — **владелец** (`ownerId`).
    - `passenger`: только поездки с **любой** бронью в `booking` по `passenger_id` (статус брони не фильтруется); время — по **`trip.start`**.
    - значение `all` для `participant` **не используется** (для «все» параметр опускают); иначе некорректное значение → **400 BAD_REQUEST**
  - `timeFilter` (опционально, по умолчанию `all`): `all` | `upcoming` | `past`
    - `upcoming`: `trip.start >= now`
    - `past`: `trip.start < now`
    - некорректное значение → **400 BAD_REQUEST**
  - параметры **`Pageable`** (`page`, `size`, при необходимости `sort`); если **`sort`** не указан — по умолчанию сортировка по **`start` по убыванию**.
- **Ответ**: **`PagedModel<TripDto>`** (стабильная JSON-форма Spring Data):
  - массив поездок: **`content`**
  - метаданные страницы: **`page`** (`size`, `number`, `totalElements`, `totalPages`)

### Получение поездки по id

- **URL**: `GET /api/trip/{id}`
- **Auth**: публичный (но есть ограничения для “старых” поездок, см. ниже)
- **Ответ**: `TripDto`
- **Ошибки**:
  - **404 NOT_FOUND**: поездка не найдена
  - **404 NOT_FOUND**: поездка “слишком старая” и недоступна текущему пользователю

### Популярные маршруты

- **URL**: `GET /api/trip/popular`
- **Auth**: публичный
- **Ответ**: список `PopularRouteDto` (`placeFrom`, `placeTo`, `c`)
- **Поведение**:
  - учитываются только поездки из будущего (по времени старта)
  - агрегация идёт **по городам** (`trips.place_from_city`, `trips.place_to_city`), а не по названию конкретного места
  - список отсортирован по `c` по убыванию

## Правила доступа к “старым” поездкам

Есть окно публичной видимости для поездок:

- **Константа**: `RECENT_TRIP_VISIBILITY_DAYS` (сейчас 7 дней)
- Если поездка **не старше** этого окна — доступна публично по `GET /api/trip/{id}`.
- Если поездка **старше** этого окна — доступна только:
  - **владельцу поездки** (`trip.ownerId == currentUser.id`)
  - **пассажиру**, у которого есть бронь на эту поездку со статусом **`BookingStatus.ACCEPTED`**

Для всех остальных (включая анонимного пользователя) старая поездка возвращается как **404 NOT_FOUND**.

## Тесты

Интеграционные тесты находятся в:

- `src/test/java/by/ladyka/poputka/controllers/TripControllerTest.java`

Там закреплены сценарии:
- фильтр “только будущие” для `/api/trip/search`
- `/api/trip/popular` учитывает только будущие поездки и сортирует по `c`
- анонимный доступ к **`GET /api/trip/owned`** → **401** Unauthorized (JSON), как для остальных защищённых ресурсов
- **`GET /api/trip/owned`**: фильтры `timeFilter`, `participant`, пагинация
- доступ к старым поездкам (owner / passenger с `ACCEPTED` / random / anonymous)
- обновление чужой поездки возвращает **404**

