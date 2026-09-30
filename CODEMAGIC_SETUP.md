# ATLAS — Codemagic setup

The repository intentionally does **not** contain the Firebase/Dify/image runtime credentials. Codemagic must inject them at build time.

Create these environment variables in the Codemagic UI (prefer a protected environment group):

## Firebase Web
- `ATLAS_FIREBASE_API_KEY`
- `ATLAS_FIREBASE_AUTH_DOMAIN`
- `ATLAS_FIREBASE_PROJECT_ID`
- `ATLAS_FIREBASE_STORAGE_BUCKET`
- `ATLAS_FIREBASE_MESSAGING_SENDER_ID`
- `ATLAS_FIREBASE_APP_ID`
- `ATLAS_FIREBASE_MEASUREMENT_ID` (optional)

## Native Google Login
- `ATLAS_GOOGLE_SERVER_CLIENT_ID`

This must be the **Web application** OAuth 2.0 client ID used as the server client ID for Credential Manager, not the Android client ID.

## AI
- `ATLAS_DIFY_URL` (optional; defaults to `https://api.dify.ai/v1`)
- `ATLAS_DIFY_KEY` (secret)
- `ATLAS_IMAGE_API_KEY` (secret)
- `ATLAS_IMAGE_MODEL` (optional; defaults to `gemini-3.1-flash-image`)

## Optional integrations
- `ATLAS_CALENDAR_API_KEY`
- `ATLAS_LINE_CHANNEL_ID`

Do not put secrets into `codemagic.yaml`, GitHub, or `config.js`.

The build step generates `android/app/src/main/assets/public/config.js` just before Gradle runs.
