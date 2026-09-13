# Sair pela tela de aceite leva a um login limpo

## Summary

Quem clica em Sair na tela de aceite de um convite volta a um login que ainda
carrega esse convite, e a próxima conta a entrar ali cai no convite de outra
pessoa. Esta mudança faz o Sair levar a um login limpo, sem mexer em quem abre o
link sem estar logado.

## Technical detail

- **Causa:** o `useLogout` chama `clearToken()` e navega para `/login`, mas a
  `AcceptInvitePage` segue montada e seu efeito, ao ver a sessão sumir, navega
  para `/login?invite=<token>` — e vence. Com o #198, o `PublicRoute` leva
  qualquer conta que entra por `/login?invite=` ao aceite.
- **A sessão não distingue "saiu" de "expirou":** os dois chamam `clearToken()`.
  Por isso o sinal vem de quem sai de propósito.
- **Correção:** `authStore` ganha `signedOutByUser` e a ação `signOut()` (limpa a
  sessão como `clearToken()` e marca `signedOutByUser = true`); `setToken()` volta
  a marca para `false`. O `useLogout` passa a usar `signOut()`. A
  `AcceptInvitePage` só redireciona para `/login?invite=` quando **não** houve
  saída explícita; caso contrário não renderiza nada e o `navigate('/login')` do
  `useLogout` fica valendo.
- **Continua igual:** quem abre o link sem sessão (inclusive quando o refresh
  inicial do `AuthBootstrap` falha) vai para `/login?invite=`.
- **Expiração no meio do uso:** se o refresh falhar numa chamada da API, o
  interceptor do `axios` já faz `window.location.href = '/login'` (recarga, sem o
  convite). Na tela de aceite isso só acontece no próprio clique em aceitar.
  Manter o convite nesse caso exige mexer no interceptor global — fora deste card.

## Scope

### In

- `signOut()` + `signedOutByUser` no `authStore`; `useLogout` usando `signOut()`.
- `AcceptInvitePage` não redireciona com convite depois de um Sair explícito.
- Testes: `authStore`, `useLogout` e `AcceptInvitePage` (Sair → sem `?invite=`;
  chegar deslogado → com `?invite=`).
- Validação no navegador do ciclo Sair → login → outra conta.

### Out

- Interceptor do `axios` (recarga para `/login` quando o refresh falha).
- O que a tela de aceite mostra a quem não é o destinatário do convite.
- `useSessionInit` (sem uso) e o vazamento JDBC nos repositórios.

## Subtasks

- [x] FE: `authStore` ganha `signOut()`/`signedOutByUser` e o `useLogout` passa a usá-lo + testes
- [x] FE: `AcceptInvitePage` não leva o convite ao login depois de um Sair explícito + testes
- [ ] Validar no navegador: Sair na tela de aceite → `/login` limpo → outra conta entra no app, não no convite
