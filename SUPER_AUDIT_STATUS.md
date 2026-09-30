# ATLAS Super Audit

Date: 2026-09-30

## Confirmed source findings
- Several UI icons were referenced but missing from the icon path map: calendar, check, expand, grid, image, link, sun.
- The "Ler" mode referenced a missing `clip` icon.
- The chat defaulted to a proxy path (`/api`) even though no API proxy endpoint existed in the Android asset project.
- Dify streaming errors were collapsed into a generic message and response parsing was fragile.
- The service worker used root-relative cache paths while the Android app loads from `file:///android_asset/public/`.
- The Android manifest did not explicitly declare a launcher icon.
- WhatsApp and Telegram were still visible in the integrations UI despite being excluded from the requested product scope.
- The release build used the debug signing configuration.
- The project has no Gradle wrapper in the supplied source package, so local compilation could not be executed in this environment.
- Firebase is dynamically imported from gstatic at runtime; this remains a production/Play Store architecture issue and should be bundled or moved to a supported native/server architecture before release.

## Corrections applied in this pass
- Added the missing SVG icon definitions.
- Replaced the missing `clip` mode icon with an existing local icon.
- Removed WhatsApp and Telegram from the visible integration UI.
- Added configured Dify connection fallback and improved Dify HTTP/stream diagnostics.
- Made service-worker cache paths relative.
- Added explicit launcher/round icons.
- Removed broad media-storage permissions and added a native WebView file chooser.
- Disabled Android backup for local conversational data.
- Added Play Store release checklist and privacy-policy template.

## Remaining release blockers
See `PLAY_STORE_RELEASE_CHECKLIST.md`.
