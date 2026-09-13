# Template padronizado de e-mail no design orgânico

## Summary

Os e-mails de confirmação de conta, redefinição de senha e convite hoje chegam
como texto sem estilo. Esta mudança dá aos três o mesmo visual do app — cores,
marca e um botão claro de ação — sem mudar quem recebe, o assunto nem os links.

## Technical detail

- **Hoje:** HTML cru em text blocks Java — `QuarkusMailAdapter` (identity:
  confirmação e redefinição) e `InvitationMailService` (organization: convite).
- **Layout:** `src/main/resources/templates/mail/layout.html` (Qute), com
  `{#insert}` para título, texto, botão e aviso final. Um template por e-mail
  (`mail/confirm-email.html`, `mail/password-reset.html`, `mail/invitation.html`)
  com `{#include mail/layout}`, injetado via `@Location` como no
  `DashboardPdfRenderer`. Nenhum Java novo em `shared/`; cada módulo segue com
  seu adapter em `infrastructure/mail/`.
- **Visual:** HTML de e-mail — tabelas, CSS inline, largura ~560px. Tema claro
  do Organic: fundo `#f5ead8`, cartão `#ebddc5`, texto `#201e1d`, botão pill
  terracota `#c67139` com texto claro, marca "N · Nexus" no topo. Título em
  Georgia/serif, corpo em sans do sistema. Sem tema escuro.
- **Links e prazos:** mesmos de hoje (24h confirmação via
  `lms.auth.confirmation-token.ttl-hours`, 1h redefinição, 7 dias convite). O
  link de confirmação passa a usar `lms.app.base-url` (padrão
  `http://localhost:5173`). Link também em texto, para quem não vê o botão.
- **Testes:** ITs com `MockMailbox` (mailer em mock no perfil de teste) checando
  destinatário, assunto, link com token e marca no HTML de cada e-mail.

## Scope

### In

- Layout Qute compartilhado + os três templates.
- `InvitationMailService` e `QuarkusMailAdapter` renderizando pelos templates.
- Link de confirmação via `lms.app.base-url`.
- ITs dos três e-mails; conferência visual no Mailpit.

### Out

- Novos e-mails ou gatilhos; configuração SMTP; notificações in-app; i18n.
- Tema escuro no e-mail; fontes web (Caprasimo/Figtree).

## Subtasks

- [x] BE: layout `mail/layout.html` + template do convite; `InvitationMailService` renderiza pelo Qute + IT com `MockMailbox`
- [x] BE: templates de confirmação e redefinição; `QuarkusMailAdapter` pelo Qute e link de confirmação via `lms.app.base-url` + ITs com `MockMailbox`
- [x] Validar no Mailpit: disparar os três e-mails e conferir visual, links e prazos
