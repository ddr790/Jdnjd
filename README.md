# ATLAS — complete source rebuild

This is the editable Android source package for ATLAS. It keeps the existing ATLAS designer/web payload and restores the Dola-style productivity layer with agenda, reminders, weather, web research, calendar handoff/sync, messaging integrations, camera, files, voice and image creation.

## GitHub build

```bash
gradle :android:app:assembleDebug
```

Or push to GitHub and run the included **Android build** workflow. It installs Gradle 8.13 and Android SDK 36 automatically.

The output is a debug-signed APK suitable for testing. Configure a real release keystore before publishing.


## Google Calendar API
A chave do Google Calendar API foi adicionada em `android/app/src/main/assets/public/config.js`. Para acessar/criar eventos em calendários privados, o ATLAS continua usando OAuth com o escopo `https://www.googleapis.com/auth/calendar`; uma API key isolada não substitui OAuth. Restrinja a chave no Google Cloud Console (APIs, Android package/SHA-1 ou HTTP referrer conforme o uso) antes de distribuição pública.


## LINE
- LINE Channel ID configured: `2011805927`.
- The Channel Secret is intentionally NOT embedded in the APK. LINE Messaging API secrets must stay on a server.
- The app includes a safe LINE share/deep-link helper (`atlasLineShare`) for opening LINE with prefilled text.
- Full bot/webhook messaging requires a server endpoint that stores the Channel Secret securely.


## ATLAS automações / mensagens
- Executor local de ações: lembretes, tarefas, abertura de URLs e mensagens.
- Notificações nativas com AlarmManager e canal ATLAS Lembretes.
- Painel de agente opcional em `ATLAS_AGENT_CONFIG.endpoint`; credenciais devem permanecer no backend.
- Mensagens/iMessage: em Android, o fallback abre o app SMS padrão; iMessage é um recurso do ecossistema Apple e não pode ser implementado como cliente iMessage nativo no Android.
- Telegram e WhatsApp permanecem deliberadamente fora desta versão.

## Play Store readiness
See `SUPER_AUDIT_STATUS.md`, `PLAY_STORE_RELEASE_CHECKLIST.md`, and `PRIVACY_POLICY.md`.
The release workflow can build an Android App Bundle and uses GitHub Secrets for a real upload keystore when configured.

## Stabilization update 1.1.0

A versão atual inclui a correção do fluxo de navegação entre telas, fechamento/Back de sheets e telas, WebView com origem local segura, arquitetura nativa de Google Login via Credential Manager/Firebase, configuração de runtime via Codemagic, auditoria de ícones e correções de integração Dify/imagem.

Consulte `BUGFIX_AUDIT_REPORT.md`, `CODEMAGIC_SETUP.md` e `FIREBASE_GOOGLE_SETUP.md` antes do release.
