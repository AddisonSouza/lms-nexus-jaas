# Sair pela tela de aceite leva a um login limpo — Delta

## Changed

- **Sair na tela de aceite não leva o convite ao login.** O `useLogout` limpava
  a sessão e navegava para `/login`, mas o efeito da `AcceptInvitePage` via a
  sessão sumir e navegava para `/login?invite=<token>` — e vencia. Com o #198, a
  próxima conta a entrar ali caía no convite de outra pessoa. Agora quem clicou
  em Sair vai para `/login` limpo.
- **`useLogout` usa `signOut()`** em vez de `clearToken()`.

## Added

- `authStore`: flag `signedOutByUser` e ação `signOut()` (limpa a sessão e marca
  a flag); `setToken()` desfaz a marca. A `AcceptInvitePage` só redireciona para
  `/login?invite=` quando a flag está `false`.
- Testes: `authStore.test.ts` (novo), casos em `useLogout`, `AcceptInvitePage` e
  `SetupShell`. Validado no navegador: Sair → `/login` limpo → outra conta entra
  no app.
- Spec viva `member-invitations`: cenário "Signing out on the accept screen".

## Unchanged

- Sessão perdida (`AuthBootstrap`, interceptor do `axios`) segue em
  `clearToken()`: quem abre o link sem sessão ainda vai a `/login?invite=`.

## Reported, not fixed

- A marca `signedOutByUser` só some no próximo login: sair e abrir outro link de
  convite sem recarregar a página não leva esse convite ao login. Raro — abrir
  um link costuma recarregar a página.
- Interceptor do `axios` recarrega para `/login` sem o convite quando o refresh
  falha no meio do uso.
