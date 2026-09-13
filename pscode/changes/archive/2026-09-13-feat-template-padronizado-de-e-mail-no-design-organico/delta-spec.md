# Template padronizado de e-mail no design orgânico — Delta

## Added

- **Layout de e-mail compartilhado.** `templates/mail/layout.html` (Qute) define
  o visual único dos e-mails transacionais no tema claro do Organic: fundo
  `#f5ead8`, cartão `#ebddc5` com raio 28px, marca "N · Nexus", título em
  Georgia, botão pill terracota `#c67139` e rodapé. HTML de e-mail — tabelas e
  CSS inline, largura máxima 560px, `color-scheme: light only` — porque Gmail e
  Outlook descartam `<style>` e fontes web. Cada e-mail preenche os blocos
  `title`, `body`, `actionLabel` e `note` e passa `actionUrl`.
- **Três templates** sobre o layout: `mail/confirm-email.html`,
  `mail/password-reset.html` e `mail/invitation.html`, injetados por
  `@Location` nos adapters de cada módulo.
- **Endereço em texto.** Abaixo do botão, o mesmo `actionUrl` aparece como link
  copiável, para clientes que bloqueiam o botão.
- **ITs com `MockMailbox`** para os três e-mails (`ConfirmEmailResourceIT`,
  `PasswordResetResourceIT`, `InvitationResourceIT`): destinatário, assunto,
  link com token e a marca no HTML.
- Specs vivas: `organic-design-system` ganha o requisito dos e-mails
  transacionais; `email-confirmation` registra a origem do link.

## Changed

- **Corpo dos e-mails sai do Java.** `QuarkusMailAdapter` (confirmação e
  redefinição) e `InvitationMailService` (convite) montavam HTML cru em text
  blocks; agora renderizam pelo Qute. Os métodos `buildConfirmationEmailBody` e
  `buildPasswordResetEmailBody` deixaram de existir.
- **Link de confirmação.** Vinha de `http://localhost:5173` fixo no código;
  passa a vir de `lms.app.base-url` (mesmo padrão), sem segredo nem host
  embutido.

## Unchanged

- Destinatários, assuntos, tokens e prazos: 24h na confirmação (via
  `lms.auth.confirmation-token.ttl-hours`), 1h na redefinição, 7 dias no
  convite. O link de redefinição segue em `lms.auth.password-reset.url`.
- Provedor e configuração SMTP, gatilhos de envio, notificações in-app.
- Sem tema escuro e sem fontes web no e-mail; sem i18n.
