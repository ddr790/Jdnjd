# ATLAS — Firebase + Google Login

## O que foi corrigido

- O ATLAS deixa de depender de `signInWithPopup()` para o Google Login no Android WebView.
- O Android usa Firebase Authentication + Credential Manager + Google ID Token.
- O WebView recebe apenas o resultado da autenticação; tokens não são expostos ao JavaScript.
- O estado da sessão é restaurado pelo Firebase nativo.
- O botão Back do Android fecha modal/painel e navega corretamente antes de sair do app.
- Agenda, Clima e Integrações agora são renderizados como telas reais, não como conteúdo de header sobre a tela de Configurações.

## Configuração externa obrigatória

No Firebase Console:

1. Habilite Authentication → Sign-in method → Google.
2. Cadastre o aplicativo Android com package name `com.atlas.assistant`.
3. Cadastre os certificados SHA-1 e SHA-256 usados pela assinatura do APK/AAB.
4. No Google Cloud Credentials, encontre o cliente OAuth 2.0 do tipo **Web application**.
5. Salve esse client ID no Codemagic como `ATLAS_GOOGLE_SERVER_CLIENT_ID`.

O client ID usado por `GetGoogleIdOption.setServerClientId()` deve ser o cliente Web, não o cliente Android.

No Codemagic, configure também as variáveis Firebase Web documentadas em `CODEMAGIC_SETUP.md`.

Nunca coloque service-account JSON, private keys ou client secrets no Git.
