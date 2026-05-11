# Authentication (JWT + Google + Apple)

The API uses **stateless** authentication: **Bearer access JWT** on protected routes, with **refresh tokens** stored server-side (hashed) and delivered to clients via **JSON** and/or **HttpOnly cookie**.

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/login` | Public | Email + password → access + refresh tokens |
| `POST` | `/api/auth/google` | Public | Google **ID token** → access + refresh (auto-register / auto-link by verified email) |
| `POST` | `/api/auth/apple` | Public | Apple **identity token** (JWT) → access + refresh (same linking rules; email only when present in token) |
| `POST` | `/api/auth/refresh` | Public | Rotates refresh token; returns new access + refresh |
| `POST` | `/api/auth/logout` | Public | Revokes refresh token (body or cookie); clears refresh cookie |
| `POST` | `/api/auth/google/link` | Bearer JWT | Link Google account to the signed-in user (Google email must match account email) |
| `POST` | `/api/auth/google/unlink` | Bearer JWT | Unlink Google after password confirmation |
| `POST` | `/api/auth/apple/link` | Bearer JWT | Link Apple to current user (email in token must match account; use «Share My Email») |
| `POST` | `/api/auth/apple/unlink` | Bearer JWT | Unlink Apple after password confirmation |

Legacy **session / form login** is disabled. Use `/api/auth/login`, `/api/auth/google`, or `/api/auth/apple`.

## Token transport (defaults)

- **Access JWT**: short-lived (default **15 minutes**), returned only in JSON as `accessToken`. Send as `Authorization: Bearer <accessToken>`.
- **Refresh token**: long-lived (default **30 days**), **rotated** on each `/api/auth/refresh`.
  - **Web (SPA, same site)**: server sets `HttpOnly` cookie `refreshToken` with path `/api/auth`, `SameSite=Lax`, `Secure` in `prod`.
  - **Mobile / non-cookie clients**: use `refreshToken` from the JSON body for `/api/auth/refresh` and `/api/auth/logout`.

The same refresh value is both cookie and JSON when both apply.

## Configuration (`application.yaml`)

```yaml
poputka:
  auth:
    jwt-secret: ${JWT_SECRET:...}           # >= 32 bytes (UTF-8 or Base64-decodable raw key)
    access-token-validity-minutes: 15
    refresh-token-validity-days: 30
    google-client-id: ${GOOGLE_CLIENT_ID:}   # Web client id (audience for Google ID tokens)
    apple-audience: ${APPLE_AUDIENCE:}       # Services ID (web) or Bundle ID (iOS) — aud in Apple identity token
    refresh-cookie-name: refreshToken
    refresh-cookie-path: /api/auth
```

## Sign in with Apple (client)

1. Получите **identity token** (JWT) из ASAuthorization / веб-флоу Apple.
2. `POST /api/auth/apple` с телом `{ "identityToken": "<jwt>" }` или `{ "identity_token": "<jwt>" }`.
3. Бэкенд проверяет подпись (JWKS `https://appleid.apple.com/auth/keys`), issuer `https://appleid.apple.com`, audience = `apple-audience`, срок.
4. Если в токене **нет email** (типично для повторных входов с «Hide My Email» без первого логина в БД) — **новый** аккаунт не создать: нужен первый вход с **Share My Email** или уже сохранённый `apple_sub`.
5. Автолинк по email — только если email есть в JWT и `email_verified` истинно (строка или boolean).

## Google Sign-In (client)

1. Obtain a Google **ID token** (OpenID Connect) via Google Identity Services (web) or the native SDK.
2. `POST /api/auth/google` with `{ "idToken": "<jwt>" }`.
3. Backend verifies issuer, audience (`google-client-id`), signature, expiry, and `email_verified`.
4. Linking rules:
   - If `google_sub` already mapped → login.
   - Else if `email_verified` and a user exists with that email → **auto-link** `google_sub`.
   - Else if `email_verified` → **create** account.
   - Otherwise → `403` (email not verified).

## Account linking

- **Implicit**: happens on `/api/auth/google` when email is verified and matches an existing local user.
- **Explicit**: `POST /api/auth/google/link` while authenticated with a valid **access** JWT; Google email must equal the account email.
- **Unlink**: `POST /api/auth/google/unlink` with `{ "password": "<local password>" }`.

## User model

`users`: `google_sub` / `google_email`, `apple_sub` / `apple_email` (уникальный nullable `*_sub`).  
Ответ профиля: `UserInfoDto.googleLinked`, `UserInfoDto.appleLinked`.  

`refresh_tokens`: **SHA-256** hash, expiry, revocation, опционально `user_agent` / `ip_address`.
