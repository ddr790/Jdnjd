# ATLAS — build and feature audit

## What was restored
- ATLAS mobile UI and existing designer preserved.
- Firebase email/password + Google authentication flow preserved.
- Native-safe Google Calendar redirect flow.
- Agenda: create/edit/delete, natural-language scheduling, ICS export.
- Android calendar handoff for created events.
- Reminders with Android notification channel + AlarmManager bridge.
- Weather via Open-Meteo + device geolocation.
- Web search with browser fallback.
- Web-page reader.
- Camera, photo/file attachments, recent photos.
- Hold-to-talk voice recognition and voice/call UI.
- ATLAS Image creator with references, style, ratio, expand/improve controls.
- Rápido / Pro selector.
- WhatsApp / Telegram / LINE / Apple Messages integration settings UI, plus Android share fallback.
- Conversation sharing via native Android share sheet when available.

## Bugs fixed in this pass
1. `webSearch()` used `await` without being declared async.
2. Web search fallback button had broken nested JavaScript quoting.
3. WebView camera/microphone permission handler granted resources when only one permission was granted; now checks each requested resource.
4. Android 13+ notification permission was missing.
5. Reminder notifications previously existed only while the web UI was running; native AlarmManager/receiver support was added.
6. Deleting a task/event now cancels its native reminder where applicable.
7. Google Calendar event end time was equal to start time; sync now uses the event duration.
8. Google Calendar native OAuth now uses redirect flow instead of popup.
9. Android calendar insertion and sharing are exposed through a small native bridge.
10. GitHub Actions now provisions Gradle 8.13 and Android SDK 36 explicitly.

## Verification
- All inline JavaScript blocks pass `node --check`.
- Android source includes launcher activity and manifest receiver.
- The workflow builds a debug APK on GitHub Actions.

## Important limitations
- WhatsApp/Telegram/LINE/Apple Messages still require the corresponding external bot/webhook/account for true server-to-server automation; the app does not fabricate those credentials.
- The image API key is client-side in the current configuration because that was the requested architecture. For public distribution, move it to a backend and rotate the exposed key.
- A production release must use a real release keystore rather than the debug signing config.
