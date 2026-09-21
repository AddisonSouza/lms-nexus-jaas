# fix: confirmação de e-mail não idempotente

## Summary

Clicar duas vezes no link de confirmação de e-mail mostra "Link inválido ou
expirado", como se algo tivesse dado errado — quando na verdade a conta foi
ativada normalmente no primeiro clique. O segundo clique passa a levar o usuário
ao login com mensagem de sucesso, igual ao primeiro.

## Technical detail

- **Causa raiz:** `ConfirmEmailService.execute()` chama
  `confirmationTokenRepository.invalidate(token)` após ativar a conta. No segundo
  clique, `findUserId(token)` não acha mais nada e lança
  `InvalidConfirmationTokenException` (400) — o branch
  `EmailAlreadyConfirmedException` (409) logo abaixo é inalcançável.
- **Correção:** remover a chamada `invalidate(token)`. A chave `ect:{token}`
  expira sozinha pelo TTL de 24h. O segundo clique então resolve o `userId`, vê
  `!user.isPendingConfirmation()` e responde 409 `EMAIL_ALREADY_CONFIRMED`.
- **Segurança:** o token não é credencial — a única ação que habilita é
  `activate()`, já guardada pelo status, e `PENDING_CONFIRMATION` só é atribuído
  em `RegisterUserService` (nunca reatribuído). Replay do token é inócuo.
- **Front-end:** `ConfirmEmailCallbackPage` já distingue o 409, mas o exibe com
  ícone de erro (`XCircle`) e sem redirecionar. Passa a renderizar o card de
  sucesso e a navegar para `/login?confirmed=true` após 2s.
- **Spec:** `REQ-EMAILCONF-02` é autocontraditória — o passo (3) manda invalidar
  o token, o que impede o próprio cenário de idempotência (409) logo abaixo. O
  passo (3) sai na `/ps:complete`.
- **Testes:** `ConfirmEmailServiceTest` afirma
  `verify(confirmationTokenRepository).invalidate(TOKEN)` — é essa asserção que
  fixa o bug; vira `verify(..., never()).invalidate(any())`.
  `ConfirmEmailResourceIT` não cobre o duplo clique.

## Scope

### In

- Remover a invalidação do token no `ConfirmEmailService`.
- Tratar o 409 como sucesso no `ConfirmEmailCallbackPage` (card verde + redirect).
- Cobrir o segundo clique em teste unitário e de integração (Testcontainers).

### Out

- Reenvio de confirmação (já funciona, limite de 3/h).
- Link expirado após 24h com conta ativa: continua 400 "Link inválido ou
  expirado" com botão de reenvio.
- Atualização da spec `email-confirmation` (o `/ps:complete` registra o delta).

## Subtasks

- [x] Remover `confirmationTokenRepository.invalidate(token)` de `ConfirmEmailService` e ajustar `ConfirmEmailServiceTest` (o `verify(...).invalidate(TOKEN)` vira `never()`)
- [x] Adicionar IT em `ConfirmEmailResourceIT`: confirmar duas vezes o mesmo token → 204 e depois 409 `EMAIL_ALREADY_CONFIRMED`
- [x] Em `ConfirmEmailCallbackPage`, renderizar o 409 como sucesso (CheckCircle, "E-mail já confirmado") e redirecionar para `/login?confirmed=true` após 2s
- [x] Cobrir o caso 409 em `ConfirmEmailCallbackPage.test.tsx` (card de sucesso + navegação para o login)
