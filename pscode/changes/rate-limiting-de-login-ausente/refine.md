# Rate limiting de login ausente
## Summary
Depois de 5 tentativas erradas em 1 minuto, o mesmo IP fica 15 minutos sem
acessar as telas de autenticação, e a tela avisa quanto tempo falta. Isso fecha
a brecha de força bruta achada no E2E (RF-02, SEC-08).
## Technical detail
- **Porta** `identity/domain/port/out/AuthRateLimiter`: `remainingBlock(ip)`, `registerFailure(ip)`, `reset(ip)`.
- **Adapter Redis** em `infrastructure/security`, no padrão de `EmailConfirmationRedisRepository`: `INCR auth-rl:fail:{ip}` com TTL 1 min; ao chegar em 5, cria `auth-rl:block:{ip}` com TTL 15 min. Limites via `@ConfigProperty`. Se o Redis cair, deixa passar e registra um warn.
- **Filtros JAX-RS** em `identity/interfaces/rest`:
  - *Request* em `/auth/**`, exceto `refresh` e `logout`: se o IP estiver bloqueado, responde 429, `Retry-After` = TTL restante e `{"error":"AUTH_RATE_LIMIT_EXCEEDED"}` antes do use case, mesmo com senha certa.
  - *Response*: 401 em `/auth/login` e 400 em `/auth/reset-password` somam; 200 em `/auth/login` zera o contador.
- **IP** = endereço remoto do Vert.x (`HttpServerRequest.remoteAddress()`). `X-Forwarded-For` só vale se `quarkus.http.proxy.proxy-address-forwarding` for ligado.
- **CORS**: incluir `Retry-After` em `quarkus.http.cors.exposed-headers`, senão o front não lê o header.
- **ITs** (Testcontainers/Dev Services): todos vêm de 127.0.0.1, então é preciso limpar as chaves `auth-rl:*` antes de cada teste de `/auth` para não quebrar o `AuthResourceIT` atual.
- **Front**: o interceptor do Axios já ignora 429. Um helper do feature `auth` lê o `Retry-After` (validado com Zod) e gera "Muitas tentativas. Tente novamente em N minutos.". O botão Entrar fica desabilitado até o prazo, sem `useState` para dados de API.
## Scope
### In
- Contagem de falhas e bloqueio por IP em `/auth/**` (exceto refresh/logout), com 429 + `Retry-After`.
- Mensagem de bloqueio em login, esqueci a senha, redefinir senha e cadastro.
- Atualização do `API_CONTRACT.md` (429 em `/auth/**`).
### Out
- Bloqueio por conta/e-mail e CAPTCHA.
- Proxy reverso e configuração de `X-Forwarded-For` em produção.
- Mudar o limite de reenvio de confirmação, que já existe por e-mail.
## Subtasks
- [ ] Porta `AuthRateLimiter` + adapter Redis (limites por config, fail-open) com teste de integração
- [ ] Filtros em `/auth/**`: bloqueio 429 + `Retry-After`, contagem login 401 / reset 400, reset no login 200, CORS exposto; ITs e isolamento do `AuthResourceIT`
- [ ] Front: mensagem com minutos restantes e botão desabilitado no login; mensagem 429 em forgot/reset/register; testes
- [ ] `API_CONTRACT.md`: documentar 429 `AUTH_RATE_LIMIT_EXCEEDED` nos endpoints de `/auth`
