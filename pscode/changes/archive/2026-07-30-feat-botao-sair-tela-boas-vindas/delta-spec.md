# Botão de sair na tela de boas-vindas — Delta

## Added

- **Saída nas telas fora da área logada**: `/welcome`, `/organizations/new` e
  `/invitations/:token/accept` passam a exibir uma barra de topo enxuta com a
  logo, a identificação do usuário e a ação de sair. Antes nenhuma delas tinha
  saída — quem caía ali ficava preso, já que ir para `/login` pela URL devolve o
  usuário autenticado de volta.
- **A ação de sair é condicionada à sessão**: `/invitations/:token/accept` é
  rota pública e pode ser aberta deslogado, quando nada é oferecido.

## Changed

- **Definição única de "sair"**: extraída para o hook `useLogout`, usado tanto
  pela barra nova quanto pelo Header da área logada. O Header chamava o axios
  direto, ignorando o `logoutUser()` que já existia. Sem mudança visual.
- **Falha no logout não escapa mais**: a rejeição da chamada ao servidor vazava
  do handler de clique como *unhandled rejection*. Agora o erro é contido e a
  sessão local é encerrada de qualquer forma — servidor fora do ar não pode
  impedir o usuário de sair.
- **Aceite de convite espera a sessão ser restaurada**: a rota é pública, não
  passa pelo `ProtectedRoute` e decidia o redirect com `isAuthenticated` ainda
  falso na primeira renderização. Quem clicava no link do convite **já logado**
  era mandado para o cadastro. Agora a tela aguarda o `isBootstrapping`.

## Notes

- Mudança só de front-end; `POST /auth/logout` já existia e não mudou.
- A correção do aceite de convite **não estava no `refine.md`**: o defeito
  apareceu na validação em navegador e foi corrigido neste card por decisão
  explícita do usuário, e não coberto pelas subtasks. Está em commit próprio
  (`fix(web): wait for the session bootstrap on the invite accept route`) com
  teste de regressão.
- As três telas perderam o wrapper `flex min-h-screen items-center justify-center`
  próprio em favor do layout compartilhado; o aceite de convite tinha um por
  estado (carregando, inválido, válido).
