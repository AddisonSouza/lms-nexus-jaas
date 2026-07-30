# Tela inicial sem forçar criação de organização

## Objetivo

Parar de obrigar todo usuário recém-cadastrado a criar uma organização. Quem
entra sem organização vinculada deve ver uma tela inicial que apresenta os
caminhos possíveis, em vez de ser jogado direto no formulário de criação.

## Comportamento esperado

- Usuário autenticado **sem** `organizationId` no JWT cai numa tela inicial de
  boas-vindas.
- A tela apresenta os caminhos: **criar uma organização** ou **entrar em uma
  organização existente via convite**.
- Criar organização continua levando ao formulário atual
  (`/organizations/new`).
- Usuário **com** `organizationId` segue com o comportamento atual (vai para a
  área logada normalmente).

## Fora de escopo

- Alterações no formulário de criação de organização.
- Alterações no fluxo de convite em si (emissão, aceite, endpoints).
- Qualquer mudança no back-end.
