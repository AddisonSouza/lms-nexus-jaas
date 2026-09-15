# [fix] validação de senha segura no backend

## Objetivo
Aplicar no back-end a mesma política de senha segura do front. Hoje uma chamada
direta à API aceita qualquer senha com 8 caracteres.

## Comportamento esperado
- Senha exige mínimo 8 caracteres, uma maiúscula, uma minúscula, um número e um
  símbolo (mesmos critérios de `passwordSchema.ts`).
- Vale para `POST /auth/register` (inclui quem chega por convite) e
  `POST /auth/reset-password`.
- Senha fraca é recusada com `422` no formato padrão `{ "errors": [...] }`.

## Fora do escopo
- Mudanças no front, medidor de força da senha.
- Histórico/expiração de senhas; senhas já cadastradas.
- Login (`POST /auth/login`) continua sem validação de força.
