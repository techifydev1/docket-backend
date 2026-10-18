# Docket Backend

REST API for Docket — a zero-knowledge family vault. The server orchestrates accounts, families, invitations and encrypted document storage, but **never sees plaintext document data or encryption keys**.

## Stack

- Java 21, Spring Boot 4.1.1 (WebMVC, Security)
- Firebase Admin — authentication (ID tokens) and Firestore persistence
- Cloudinary — storage for client-encrypted document blobs (authenticated delivery, signed URLs, 10 MB upload cap)
- Brevo — transactional email for verification and reset OTPs
- Resilience4j — per-IP rate limiting on sensitive endpoints (register, invite, password reset, OTP)

## Running locally

Requirements: Java 21, a Firebase service account at `src/main/resources/serviceAccountKey.json`.

```bash
./mvnw spring-boot:run
```

The app runs on port `8080` with the `dev` profile active by default. Local configuration (Brevo / Cloudinary keys) lives in `src/main/resources/application-dev.properties`.

### Docker

```bash
docker build -t docket-backend .
docker run -p 8080:8080 \
  -e REDIS_HOST=... -e REDIS_PORT=... -e REDIS_USERNAME=... -e REDIS_PASSWORD=... \
  -e BREVO_API_KEY=... -e BREVO_FROM_NAME=... -e BREVO_FROM_EMAIL=... \
  -e CLOUDINARY_NAME=... -e CLOUDINARY_API_KEY=... -e CLOUDINARY_API_SECRET=... \
  docket-backend
```

The `prod` profile reads all secrets from environment variables.

## API overview

All endpoints are under `/api`. Authenticated routes expect `Authorization: Bearer <firebase-id-token>`.

| Group | Endpoints |
| --- | --- |
| Auth | `POST /auth/register`, `POST /auth/verify`, `POST /auth/send-verification-email`, `POST /auth/init-password-reset`, `POST /auth/confirm-password-reset` |
| User | `GET /user`, `POST /user` |
| Family | `GET /family/all`, `POST /family` |
| Documents | `POST /document/sign`, `POST /document/confirm`, `GET /document/{familyId}`, `GET /document/{familyId}/{docId}/url`, `DELETE /document/{familyId}/{docId}` |
| Invitations | `POST /invitation/initiate`, `POST /invitation`, `GET /invitation`, `POST /invitation/{id}/accept`, `POST /invitation/{id}/decline` |
| Recovery | `POST /recovery/backup`, `POST /recovery/request`, `POST /recovery/confirm` |
| Health | `GET /healthz` |

## How the zero-knowledge model maps to the API

- **Documents**: the client encrypts each file with the family key, uploads the ciphertext to Cloudinary via a signed upload (`POST /document/sign`), then persists metadata (`POST /document/confirm`). Metadata stored in Firestore is itself encrypted client-side. Downloads return a signed Cloudinary URL; the server only ever handles ciphertext.
- **Invitations**: `initiate` returns the invitee's public key; the inviter wraps the family key for them client-side before calling `POST /invitation`. The server stores the wrapped key, never the key itself.
- **Recovery**: the server stores a phrase-encrypted key backup (`POST /recovery/backup`). `request`/`confirm` are OTP-gated, and the backup blob is only released to a holder of the recovery phrase.
- **Roles**: family membership roles (`OWNER`, `ADMIN`, `MEMBER`, `VIEWER`) gate upload, invite, and delete permissions server-side.
