# Login com convite chega à tela de aceite

## Summary

Quem abre um link de convite sem estar logado e entra pela tela de login deve
cair na tela de aceite desse convite. Hoje cai na página de boas-vindas quando o
convite já não está pendente, e a pessoa nunca vê o motivo (cancelado, reenviado
ou expirado).

## Technical detail

- **Causa:** `PublicRoute` (`components/shared/PublicRoute.tsx`) renderiza
  `<Navigate to="/" replace />` assim que `isAuthenticated` vira `true`, passando
  por cima do `navigate('/invitations/<token>/accept')` do `useLogin`. Em `/`, o
  `RootRedirect` só leva ao aceite se houver convite **pendente**, o que
  escondia o bug.
- **Correção:** com sessão, o `PublicRoute` lê `?invite=` da URL
  (`useSearchParams`) e redireciona para
  `/invitations/${encodeURIComponent(token)}/accept`. Sem o parâmetro, segue para
  `/`. O `useLogin` fica como está: os dois caminhos passam a apontar para o
  mesmo destino.
- **Alcance:** vale para todas as rotas envolvidas pelo `PublicRoute` (`/login`,
  `/register`, `/forgot-password`, `/reset-password`), inclusive para quem já
  chega logado, com a sessão restaurada pelo refresh.
- **Por que os testes não pegaram:** `LoginPage.test.tsx` renderiza a página sem
  o guard, e o `PublicRoute` não tem testes. A correção vem com testes do guard e
  um teste de login montado com o guard real.

## Scope

### In

- `PublicRoute` honra `?invite=` + testes (com e sem parâmetro, bootstrapping).
- Teste de login → aceite com o `PublicRoute` real na árvore de rotas.
- Validação no navegador com convite cancelado.

### Out

- Fluxo de cadastro por `?invite=` (`RegisterPage`/`useRegister` ignoram o parâmetro).
- Regras de convite (estados, reenvio, cancelamento) e a prévia `GET /invitations/{token}`.
- Aviso "Convite enviado para …" que permanece após cancelar (visual, #77).

## Subtasks

- [x] FE: `PublicRoute` leva ao aceite quando há `?invite=` + testes do guard
- [x] FE: teste de login com `?invite=` montado com o `PublicRoute` real, chegando ao aceite
  — **descartado na implementação:** com `<MemoryRouter>` a navegação é síncrona e
  o teste passa até sem a correção; com `createMemoryRouter` (o data router da app)
  ele quebra no jsdom (`AbortSignal` incompatível com o `Request` do Node), com ou
  sem a correção. A regressão fica coberta pelos testes do `PublicRoute` (2 de 6
  falham no guard antigo) e o caminho completo, pela validação no navegador.
- [x] Validar no navegador: link de convite cancelado, deslogado → login → tela de aceite com a mensagem de cancelado
