# [feat] botão de voltar ao login nas telas públicas

## Objetivo
Dar uma saída explícita de volta à tela de login nas telas públicas de
autenticação, hoje sem navegação de retorno — o usuário só volta pelo botão do
navegador ou digitando a URL.

## Comportamento esperado
- As telas públicas (`/register`, `/forgot-password`, `/reset-password`,
  `/confirm-email`) passam a exibir um controle de navegação consistente que
  leva a `/login`.
- O controle aparece também nos estados sem formulário dessas telas (e-mail
  enviado, link inválido, link expirado).

## Fora de escopo
- Telas autenticadas (AppShell).
- Fluxo de onboarding (`/welcome`, `/organizations/new`) e o aceite de convite,
  que já têm saída própria via MinimalHeader.
- Qualquer mudança de regra de autenticação ou de rota.
