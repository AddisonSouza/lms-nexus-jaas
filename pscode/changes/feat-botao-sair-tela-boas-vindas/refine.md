# Botão de sair na tela de boas-vindas

## Summary

As telas que ficam fora da área logada — boas-vindas, criação de organização e
aceite de convite — não têm barra de topo e, portanto, nenhuma forma de sair da
conta. Quem cai nelas fica preso: ir para `/login` pela URL devolve o usuário de
volta, porque ele já está autenticado. As três passam a exibir uma barra de topo
enxuta com a logo, a identificação do usuário e a ação de sair.

## Technical detail

- `MinimalHeader` em `components/layout/`: logo Nexus, identificação do usuário e
  "Sair". A ação de sair só aparece quando há sessão — `/invitations/:token/accept`
  é rota **pública** (sem `ProtectedRoute`) e pode ser aberta deslogado.
- Aplicado por um layout route (`SetupShell`) que renderiza o `MinimalHeader` e
  centraliza o `Outlet`, em vez de repetir a barra em cada página. As três telas
  hoje trazem o próprio wrapper `flex min-h-screen items-center justify-center`,
  que sai em favor do shell.
- `useLogout` em `features/auth/hooks/`, sobre o `logoutUser()` que já existe em
  `auth-api.ts`: encerra a sessão, limpa o token e navega para `/login`. O
  `Header` do `AppShell` passa a usá-lo — hoje ele repete a chamada axios crua,
  ignorando o `logoutUser()`.
- `AcceptInvitePage` tem três estados full-screen (carregando, convite inválido,
  convite válido); todos passam a viver dentro do shell.

## Scope

### In

- `MinimalHeader` e o layout route `SetupShell`.
- Hook `useLogout` compartilhado, com o `Header` do `AppShell` migrado para ele.
- Aplicação às três telas: `/welcome`, `/organizations/new` e
  `/invitations/:token/accept`.
- Testes de componente para o hook, o header mínimo e o comportamento deslogado.

### Out

- Conteúdo das três telas: cards, formulários, campo de convite e avisos.
- O `Header` do `AppShell` além da troca para o hook — sem mudança visual.
- Alternador de tema e sino de notificações no header mínimo.
- Qualquer mudança no back-end; `POST /auth/logout` já existe e não muda.

## Subtasks

- [ ] Extrair `useLogout` em `features/auth/hooks/` sobre o `logoutUser()`
      existente e migrar o `Header` do `AppShell` para ele, sem mudança visual.
- [ ] Criar `MinimalHeader` (logo, identificação do usuário e "Sair", com a
      ação de sair condicionada à sessão) e o layout route `SetupShell` que o
      aplica e centraliza o conteúdo.
- [ ] Colocar `/welcome`, `/organizations/new` e `/invitations/:token/accept`
      sob o `SetupShell`, removendo os wrappers full-screen próprios das três
      telas.
- [ ] Cobrir com testes: `useLogout` encerrando a sessão e indo para `/login`,
      o `MinimalHeader` sem a ação quando deslogado, e a saída funcionando a
      partir da `/welcome`.
