# [fix] exibir nome do usuário no lugar do UUID no header

## Summary
O canto superior direito mostra um pedaço do ID interno do usuário. Ele passa a
mostrar o nome da pessoa logada (ou o e-mail, se não houver nome).

## Technical detail
- Causa: o access token só carrega `sub`, `org` e `groups`; `Header.tsx:30` e
  `MinimalHeader.tsx:25` exibem `userId.slice(0, 8)`.
- API: `TokenGeneratorPort`/`JwtTokenService` passam a emitir as claims `name`
  (`User.fullName`) e `email`. `AuthenticateService` já tem o `User`;
  `RefreshTokenService` e `SwitchOrganizationService` carregam via
  `UserRepository.findById` (mesmo módulo `identity`).
- Web: `authStore` lê `name`/`email` do payload → `userName`/`userEmail`,
  zerados no `signOut`/`clearToken`.
- Headers: exibem `userName ?? userEmail`, com `truncate` + `max-w` e o valor
  inteiro no `title`. Nunca o UUID.
- Nome editado só reflete no próximo refresh (TTL do token: 15 min) — aceito.

## Scope
### In
- Claims `name` e `email` no access token (login, refresh, troca de organização).
- `Header` e `MinimalHeader` exibindo o nome, com fallback para e-mail.
- Testes unitários da API, do `authStore` e do `MinimalHeader`; nota no `API_CONTRACT.md`.

### Out
- Endpoint `GET /auth/me`.
- Layout do menu de usuário, edição de perfil, avatar/foto.

## Subtasks
- [x] API: emitir claims `name` e `email` no access token nos três fluxos + testes
- [x] Web: `authStore` expõe `userName` e `userEmail` a partir do token + teste
- [x] Web: `Header` e `MinimalHeader` exibem nome (fallback e-mail), truncado + teste
- [x] Docs: documentar as claims do access token no `API_CONTRACT.md`
