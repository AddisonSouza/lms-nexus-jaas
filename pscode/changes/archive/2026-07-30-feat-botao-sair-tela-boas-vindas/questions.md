# Questions

- [x] **Quais telas ganham a saída?** As três que ficam fora do `AppShell`:
  `/welcome`, `/organizations/new` e `/invitations/:token/accept`. Todas prendem
  o usuário autenticado do mesmo jeito.
- [x] **Que forma tem a saída?** Um header mínimo compartilhado — logo Nexus,
  identificação do usuário e "Sair" — reutilizado pelas três telas, em vez de um
  botão solto em cada uma.
- [x] **E a duplicação do logout?** Extrair `useLogout` sobre o `logoutUser()`
  que já existe em `auth-api.ts` e fazer o `Header` do `AppShell` usá-lo também.
  Hoje o `Header` repete a chamada axios crua.

## Aprendido na análise do código

- `/invitations/:token/accept` é rota **pública** (não passa por
  `ProtectedRoute`): redireciona para `/register` quando não há sessão. O header
  mínimo só pode oferecer "Sair" quando o usuário está autenticado.
