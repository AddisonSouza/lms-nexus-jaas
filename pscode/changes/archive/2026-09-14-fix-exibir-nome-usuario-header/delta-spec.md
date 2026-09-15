# Nome do usuário no header no lugar do UUID — Delta

## Added

- **Claims `name` e `email` no access token.** `JwtTokenService` emite
  `name` (`User.fullName`) e `email` ao lado de `sub`, `groups` e `org`, no
  login, no refresh e na troca de organização. `TokenGeneratorPort` passa a
  receber o `User` de domínio em vez do `userId`.
- **`authStore` expõe `userName` e `userEmail`**, lidos do payload no
  `setToken` e zerados em `signOut`/`clearToken`.
- **Testes:** `AuthResourceIT` confere `name`/`email` nos três fluxos;
  `RefreshTokenServiceTest` cobre usuário inexistente; `authStore.test.ts` cobre
  nome acentuado e token sem nome; `MinimalHeader.test.tsx` cobre nome e
  fallback para e-mail.
- Specs vivas: `authentication` (REQ-AUTH-05 e REQ-AUTH-07) e `app-layout`
  (requisito do Header).

## Changed

- **Header e MinimalHeader.** Exibiam `userId.slice(0, 8)…`; agora exibem
  `userName ?? userEmail`, truncado (`max-w-48`) com o valor inteiro no
  `title`. O UUID não aparece mais.
- **Refresh e troca de organização carregam o usuário** via
  `UserRepository.findById`. Usuário inexistente encerra a sessão com 401
  (`TokenNotFoundException`) — antes o token era emitido só com o id.
- **Decodificação do JWT no front.** `atob` direto virou base64url + UTF-8
  (`TextDecoder`): antes um nome acentuado saía corrompido ou lançava erro, o
  que zerava todas as claims (role e org inclusive).
- `API_CONTRACT.md` lista as novas claims.

## Unchanged

- Sem endpoint `GET /auth/me`. TTL do access token segue 15 min: um nome
  alterado só aparece no próximo token.
- Layout do menu de usuário, edição de perfil, avatar.
