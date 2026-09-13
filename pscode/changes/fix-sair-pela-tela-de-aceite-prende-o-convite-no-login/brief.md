# Sair pela tela de aceite prende o convite no login

## Objetivo

Sair da conta estando na tela de aceite de um convite deve levar a um `/login`
limpo. A próxima pessoa a entrar não pode cair no convite de outra.

## Comportamento esperado

- Logado na tela de aceite, clicar em Sair → `/login`, sem `?invite=`.
- Quem abre o link do convite **sem sessão** continua indo a
  `/login?invite=<token>` e, depois de entrar, ao aceite (#198).

## Comportamento atual (causa)

O "Sair" do `MinimalHeader` chama o `useLogout`, que limpa a sessão e navega
para `/login`. A `AcceptInvitePage` segue montada: o efeito dela vê a sessão
sumir e navega para `/login?invite=<token>`, e essa navegação vence. Com o #198,
o `PublicRoute` leva qualquer conta que entra por `/login?invite=` ao aceite
desse token — todo login cai no convite de outra pessoa, e sair por ali de novo
repete o ciclo.

Reproduzido no teste do #204 (PR #209): o admin `b54436e0…` caiu no convite
`7e1513ad…`, endereçado ao convidado.

## Correção decidida

A `AcceptInvitePage` só redireciona para `/login?invite=` quem **abriu o link
sem sessão**. Quem estava logado e clicou em Sair vai para `/login` limpo.

## Fora de escopo

- O que a tela de aceite mostra a quem não é o destinatário.
- O vazamento JDBC de `getResultStream().findFirst()` nos repositórios.
