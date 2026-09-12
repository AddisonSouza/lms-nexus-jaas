# Login com convite chega à tela de aceite — Delta

## Changed

- **`PublicRoute` honra o `?invite=`.** Com sessão, levava todo usuário a `/`,
  passando por cima do `navigate('/invitations/<token>/accept')` do `useLogin`.
  Em `/`, o `RootRedirect` só leva ao aceite se o convite ainda estiver
  pendente, então um link cancelado, reenviado ou expirado caía em `/welcome`
  sem explicação. Agora, com `?invite=<token>` na URL, o guard leva a
  `/invitations/<token>/accept`; sem o parâmetro (ou com ele vazio), segue
  para `/`.
- A regra vale para todas as rotas do `PublicRoute` (`/login`, `/register`,
  `/forgot-password`, `/reset-password`), inclusive para quem já chega logado
  com a sessão restaurada pelo refresh.

## Added

- `PublicRoute.test.tsx` (6 casos). Os casos "logado com `?invite=`" e
  "`/register?invite=`" falham no guard antigo.

## Unchanged

- `useLogin` continua navegando para o aceite: os dois caminhos apontam para o
  mesmo destino e a corrida deixa de importar.
- O cadastro por `?invite=` segue ignorando o parâmetro.

## Not done

- **Teste de login integrado** (subtask 2, descartada): com `<MemoryRouter>` a
  navegação é síncrona e o teste passa até sem a correção; com
  `createMemoryRouter` (o data router da app) ele quebra no jsdom deste projeto
  (`AbortSignal` incompatível com o `Request` do Node).

## Reported, not fixed

- **#204** — membro removido não consegue voltar por convite: o aceite insere
  outra linha e esbarra no `uq_member (organization_id, user_id)`, que ignora o
  `deleted_at`, e responde 500. Achado no teste manual deste card.
