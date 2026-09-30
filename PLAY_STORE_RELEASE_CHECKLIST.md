# ATLAS — Play Store Release Checklist

## Source audit completed
- [x] Target SDK 36
- [x] Compile SDK 36
- [x] INTERNET permission
- [x] Launcher activity exported correctly
- [x] Launcher icon declared
- [x] Firebase config path is relative
- [x] Broken icon definitions repaired
- [x] WhatsApp/Telegram removed from the product UI
- [x] Dify request diagnostics improved
- [x] Service-worker paths made relative
- [x] Android backup disabled because the app stores conversational data locally
- [x] Broad READ_MEDIA_* permissions removed; file selection uses the system picker through WebView

## Release blockers that require account/backend work
- [ ] Move Dify API key out of the APK and use a server-side proxy before public production.
- [ ] Move image-generation API key out of the APK and rotate the exposed key before public production.
- [ ] Configure a real release/upload keystore; never ship the debug signing key.
- [ ] Build and upload an Android App Bundle (.aab), not only an APK.
- [ ] Publish a public privacy-policy URL and complete Play Data Safety accurately.
- [ ] Complete Play Console app-content declarations and reviewer access if login is required.
- [ ] If this is a new personal developer account, complete the required closed test with 12 testers for 14 continuous days before production access.
- [ ] Complete Play Console developer/package verification requirements.
