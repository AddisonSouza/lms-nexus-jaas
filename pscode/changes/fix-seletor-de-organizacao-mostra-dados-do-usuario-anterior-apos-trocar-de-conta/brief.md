# Seletor de organização mostra dados do usuário anterior após trocar de conta

## Objective

Depois de sair de uma conta e entrar com outra, o app deve mostrar só os dados da
nova conta — a começar pelo seletor de organização.

## Expected behavior

- Sair e entrar com outra conta → o seletor mostra a organização ativa da nova
  conta e lista apenas as organizações dela.
- Conta sem organização vê o estado vazio ("Sem organização" e "Você ainda não
  pertence a nenhuma organização.").
- Nenhuma outra tela (turmas, notificações etc.) mostra dados da conta anterior.

## Current behavior (cause)

O `useLogout` limpa o `authStore`, mas não o cache do React Query (`staleTime`
de 60s). A lista de organizações usa a chave `organizationKeys.lists()`, que não
depende do usuário, então a nova conta recebe a lista da anterior. O seletor não
acha o `organizationId` do novo token nela e mostra "Sem organização".

Reproduzido: `alunoteste@gmail.com` (ADMIN_ORG de "ee") entrou depois de
`gp77-guest` e viu "Escola GP77 · Aluno".

## Out of scope

- Mudanças visuais no seletor e no fluxo de criar organização.
- Chaves de query por usuário.
