# Documents (User verification docs)

This document describes the user documents feature exposed by `DocumentController` under `/api/documents`.

## Concepts

- **User document**: a record describing a document (type/description/expiration date) plus uploaded files.
- **Document files**: binary attachments uploaded to a document via multipart upload.

## Statuses

`UserDocument.status` is `DocumentStatus`:

- `DRAFT`: created/edited by user, not submitted for review yet
- `REVIEW`: submitted by user, waiting for moderation
- `DECLINE`: rejected by moderation (user can update and resubmit)
- `APPROVE`: approved by moderation

Allowed transitions in current backend logic:

- `DRAFT` → `REVIEW` (via submit)
- `DECLINE` → `REVIEW` (via submit)
- `DECLINE` → `DRAFT` (via update)
- `DRAFT` → `DRAFT` (via update)

## Authentication & ownership

All endpoints use the authenticated user (via `ApplicationUserDetails`) and operate on **their** documents only.
Update/submit/upload are rejected if the document does not belong to the current user.

## API

Base path: `/api/documents`

### List documents

`GET /api/documents`

Response: `200 OK`, JSON array of `UserDocumentDto`.

### Create document

`PUT /api/documents/create`

Body (`application/json`): `UserDocumentRequestCreateDto`

```json
{
  "type": "OTHER",
  "description": "Passport (main page)",
  "expirationDate": "2030-12-31"
}
```

Response: `201 Created`, `UserDocumentDto`.

Notes:
- Created document starts in `DRAFT`.

### Update document

`PUT /api/documents/update`

Body (`application/json`): `UserDocumentRequestUpdateDto`

```json
{
  "id": "123",
  "type": "PASSPORT",
  "description": "New description",
  "expirationDate": "2031-01-01"
}
```

Response: `202 Accepted`, `UserDocumentDto`.

Allowed only when status is `DRAFT` or `DECLINE`.

### Upload document files

`POST /api/documents/upload/{documentId}`

Multipart form-data:
- field name: `files`
- supports multiple files: `files=<file1>, files=<file2>, ...`

Response: `201 Created`, JSON array of strings (`List<String>`) where each element is the stored file name / URL returned by `FileService`.

Notes:
- Upload is allowed only when document status is `DRAFT` or `DECLINE`.
- Upload **does not** change document status. Use submit to move to `REVIEW`.

### Submit document for review

`POST /api/documents/submit?documentId=<id>`

Response: `202 Accepted`, `UserDocumentDto` with status `REVIEW`.

Allowed only when status is `DRAFT` or `DECLINE`.

