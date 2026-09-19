# fix: rate limiting de login ausente — Delta

## Added
- **Limite de tentativas em `/auth` (SEC-08, RF-02).** 5 falhas de um mesmo IP
  em 1 minuto fecham toda a área de autenticação para ele por 15 minutos:
  `429` com `{"error":"AUTH_RATE_LIMIT_EXCEEDED"}` e `Retry-After` em segundos.
  `POST /auth/refresh` e `POST /auth/logout` ficam de fora — são a sessão de
  quem já entrou, e derrubá-los puniria quem divide o IP com o atacante.
- **Porta `AuthRateLimiter`** (`remainingBlock`/`registerFailure`/`reset`) com
  adapter Redis: `auth-rl:fail:{ip}` conta dentro da janela e expira sozinha;
  `auth-rl:block:{ip}` guarda o bloqueio e carrega o tempo restante no TTL.
  Limites por `AUTH_RATE_LIMIT_MAX_FAILURES`, `_WINDOW_SECONDS` e
  `_BLOCK_SECONDS`. **Fail-open**: Redis fora libera a requisição e loga um
  warn, em vez de trocar a brecha por uma indisponibilidade total.
- **Filtro de request** responde 429 **antes do use case** — durante o bloqueio
  nem a senha certa entra, senão bastaria acertar na tentativa seguinte para
  escapar do limite. **Filtro de response** conta o `401` de `/auth/login` e o
  `400` de `/auth/reset-password`; o `200` do login zera a contagem.
- **`Retry-After` exposto no CORS** (`quarkus.http.cors.exposed-headers`), sem o
  que o navegador esconde o header e a tela não sabe quanto tempo falta.
- **Mensagem de bloqueio nas telas de autenticação**: login, esqueci a senha,
  redefinir senha e cadastro mostram "Muitas tentativas. Tente novamente em N
  minutos", com N lido do `Retry-After` (validado por Zod) e arredondado para
  cima. No login o botão Entrar fica desabilitado e reabre sozinho ao fim da
  contagem.
- **Callback `ClearAuthRateLimit`** (`META-INF/services`) zera as chaves
  `auth-rl:*` antes de cada teste: todos os ITs chegam de 127.0.0.1, então sem
  isso a primeira classe a acumular falhas bloquearia `/auth` para as demais e a
  suíte quebraria por ordem de execução.
- Testes: `AuthRateLimiterRedisAdapterIT` (6), `AuthRateLimitIT` (6),
  `rateLimitSchema` (7), `getRegisterError` (4) e os casos de 429 em login,
  esqueci a senha e redefinir senha.

## Changed
- **Tela "esqueci minha senha"** passa a exibir erro. Ela só tratava o caminho
  feliz — a rota responde `204` mesmo para e-mail inexistente —, então uma
  recusa deixava o botão parecendo inerte.
- **`POST /auth/register`, `/auth/login`, `/auth/reset-password`,
  `/auth/forgot-password` e `/auth/resend-confirmation`** ganham `429` no
  `API_CONTRACT.md`, com a regra descrita uma vez no topo do módulo `identity`.
  O `429` por e-mail do reenvio (`RESEND_RATE_LIMIT_EXCEEDED`) continua valendo
  em paralelo.

## Removed
- Nada.

## Conhecido, fora deste card
- O limite é por IP. Quem sai por NAT (escola, empresa) compartilha a contagem;
  bloqueio por conta e CAPTCHA ficaram fora de escopo por decisão do refine.
- Atrás de proxy reverso é preciso ligar
  `quarkus.http.proxy.proxy-address-forwarding`, senão o IP visto é o do proxy e
  o bloqueio atinge todo mundo de uma vez. A configuração de produção não faz
  parte deste card.
