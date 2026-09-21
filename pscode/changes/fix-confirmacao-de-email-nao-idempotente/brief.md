# fix: confirmação de e-mail não idempotente

**Severidade:** alto · RF-04 · origem: bateria E2E 15/09/2026

## Objetivo

Aceitar o segundo clique no link de confirmação de e-mail sem erro. Hoje a API
responde 400 e a tela mostra "Link inválido ou expirado", mesmo com a conta já
ativa.

## Comportamento esperado

- Conta já `ACTIVE` → a API responde 409 `EMAIL_ALREADY_CONFIRMED` (conforme a
  spec `email-confirmation`), e o front trata o caso levando ao login com
  mensagem de sucesso.
- Primeiro clique continua retornando 204 e ativando a conta.

## Fora do escopo

- Reenvio de confirmação (já funciona, com limite de 3/h).
