# fix: createdAt do convite com fuso errado

**Severidade:** alto · RF-06 · origem: bateria E2E 15/09/2026

## Objetivo

Corrigir o `createdAt` do convite, que sai com hora local rotulada como UTC
(`00:32Z` enquanto `expiresAt` vem `03:32Z`). A lista de convites mostra
"Enviado em 14/09" para um convite criado em 15/09.

## Comportamento esperado

- `createdAt` e `expiresAt` do mesmo convite referem-se ao mesmo instante e ao
  mesmo fuso; a data exibida na tabela é a data real do envio.
- O mesmo padrão é verificado em `joinedAt` de membros e no `createdAt` de
  submissões (o `PUT` devolve `createdAt: null`).

## Fora do escopo

- Migração/correção de registros antigos já gravados com o fuso errado.
