# Template padronizado de e-mail no design orgânico

## Objective

Trocar o HTML cru embutido em strings Java por um template de e-mail único, com
a identidade visual do design orgânico. Hoje cada e-mail monta o próprio HTML,
sem estilo algum.

## Expected behavior

- Os três e-mails atuais passam a sair de um layout base compartilhado (Qute):
  - confirmação de e-mail e redefinição de senha (`QuarkusMailAdapter`);
  - convite para organização (`InvitationMailService`).
- O layout tem cabeçalho com a marca, corpo, botão de ação e rodapé
  consistentes, nas cores e fontes do design orgânico.
- Destinatários, assuntos, links e prazos de expiração continuam os mesmos. O
  link de confirmação, hoje fixo em `http://localhost:5173`, passa a vir de
  `lms.app.base-url` (mesmo endereço padrão).

## Out of scope

- Novos e-mails ou novos gatilhos de envio.
- Mudança no provedor ou na configuração SMTP.
- E-mails de notificação in-app.
- Internacionalização.
