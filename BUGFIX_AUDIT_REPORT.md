# ATLAS — Bugfix Audit & Stabilization Report

## Corrigido nesta rodada

- Navegação entre Configurações, Agenda, Clima, Integrações e demais telas agora usa uma pilha de navegação, evitando que a tela anterior permaneça visível por baixo.
- Telas secundárias receberam cabeçalho com voltar e o botão Back do Android foi integrado ao mesmo fluxo.
- Bottom sheets/modais agora bloqueiam o scroll da tela de fundo, têm botão de fechar e são removidos corretamente ao sair.
- Android 13+ usa `OnBackInvokedDispatcher`; versões anteriores usam `onBackPressed` com o mesmo roteamento para o JavaScript.
- O WebView agora usa `WebViewAssetLoader` com origem HTTPS local (`appassets.androidplatform.net`) em vez de `file://`.
- Navegação externa do WebView é aberta fora do WebView, evitando que páginas externas herdem a ponte `ATLASNative`.
- Login Google Android foi reestruturado para Credential Manager + Firebase Authentication nativo; o WebView recebe apenas o resultado da autenticação, nunca tokens.
- O fluxo de sessão Firebase foi centralizado e a persistência web usa `browserLocalPersistence` quando o Firebase Web está disponível.
- Mensagens antigas de fallback para Claude foram removidas.
- O motor Dify recebe configuração do build e apresenta erros específicos de configuração, HTTP 401/403/404/429 e indisponibilidade.
- Geração de imagem recebeu parsing de múltiplos formatos de resposta e tratamento de erros 400/401/403/ausência de imagem.
- Registro de ícones ganhou fallback seguro e os ícones ausentes identificados anteriormente foram adicionados ao registro.
- Ícone Android compatível com o `AndroidManifest` foi restaurado como `drawable/icon_192.png`.
- Versão exibida na interface foi atualizada para 1.1.0.
- Arquivos de configuração rastreados no Git permanecem sem chaves reais; valores de runtime são gerados pelo Codemagic.

## Validações realizadas no ambiente disponível

- 4 blocos de JavaScript inline: `node --check` aprovado.
- `codemagic.yaml`: YAML validado com PyYAML.
- Auditoria de ícones usados por `ic(...)`: 33 nomes analisados, nenhuma referência ausente.
- Busca por referências a `claude.ai`, `claude.use` e a mensagem `The requested action is invalid.` no código principal: nenhuma ocorrência.
- Busca por padrões de chaves Firebase/Dify/Google expostas no código-fonte: nenhuma ocorrência.
- Estrutura do projeto, manifest e arquivos de configuração conferidos.

## Limitação conhecida de validação

O ambiente desta revisão não possui Android SDK/Gradle configurados nem um dispositivo Android conectado. Portanto, não foi possível executar localmente o APK/AAB ou validar a autenticação Google de ponta a ponta em um aparelho físico.

Para o login Google Android funcionar no build real, o Codemagic precisa receber `ATLAS_GOOGLE_SERVER_CLIENT_ID` com o OAuth 2.0 Client ID da aplicação Web, além da configuração correspondente no Firebase/Google Cloud e dos SHA-1/SHA-256 do certificado de assinatura.

O fluxo nativo foi implementado para usar Credential Manager + Firebase `signInWithCredential`; a validação final ainda precisa ser feita em Android real após essas configurações externas.
