# Botão de sair na tela de boas-vindas

## Objetivo

Dar uma saída ao usuário que cai na `/welcome`. A tela fica fora do `AppShell`
e portanto não tem Header, que é onde vive o botão "Sair". Hoje o usuário sem
organização não consegue encerrar a sessão: ir a `/login` pela URL também não
resolve, porque o `PublicRoute` devolve o usuário autenticado para `/`, que
redireciona de volta para `/welcome`. Na prática, só limpando o storage.

## Comportamento esperado

- Ação de sair visível na `/welcome`.
- Ao acionar, a sessão é encerrada e o usuário vai para `/login`.
- O usuário deslogado consegue entrar com outra conta normalmente.

## Fora de escopo

- O resto da tela de boas-vindas: cards, campo de convite e o aviso.
- O `AppShell`, que já tem o botão "Sair" no Header.

## A decidir no refino

- A `/organizations/new` e a `/invitations/:token/accept` estão no mesmo caso
  (também fora do `AppShell`) — entram neste card ou viram outro?
