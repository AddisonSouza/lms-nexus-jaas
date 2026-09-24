# chore: preparar deploy de produção na OCI free tier — Delta

## Added

- **Produção no ar em `https://lmsnexus.com.br`** — VM ARM (A1.Flex 2 OCPU /
  12 GB, Always Free) em `sa-vinhedo-1`, com build e execução via
  `infra/docker-compose.prod.yml`: `mysql`, `redis`, `api`, `web` e `certbot`.
  Só o `web` publica portas (80/443); banco, cache e API ficam na rede interna.
- **Origem única atrás do nginx de borda** (o próprio container `web`): `/`
  serve a SPA e `/api/` faz proxy para o Quarkus removendo o prefixo — nenhum
  `@Path` mudou e o front usa `VITE_API_URL=/api`.
- **TLS Let's Encrypt** emitido por `init-letsencrypt.sh` (staging → real) e
  renovado pelo serviço `certbot`, com reload do nginx a cada 6 h. O `www` entra
  no certificado quando resolve e recebe 301 para o apex; HSTS com
  `includeSubDomains`.
- **Imagens de produção** multi-stage para arm64: API em fast-jar sobre JRE 21
  rodando como UID 1001, web como build Vite servido pelo nginx.
- **Perfil `%prod`**: storage no OCI Object Storage (S3-compatible, path-style,
  Customer Secret Key), SMTP com STARTTLS obrigatório, `lms.app.base-url`
  declarado (antes só `defaultValue` → links de e-mail para localhost),
  `proxy-address-forwarding` e `quarkus-smallrye-health` para o healthcheck.
- **Backup** `mysqldump` noturno em cron com retenção de 7 dias; restore validado
  em banco descartável.
- **Runbook** `docs/DEPLOY.md` e `infra/.env.prod.example`, corrigidos contra o
  deploy real (firewall antes do `REJECT` da imagem, dono das chaves JWT,
  permissões do backup).

## Changed

- **Cookie de refresh atrás do proxy**: a API segue emitindo `Path=/auth`; o
  nginx reescreve para `/api/auth`. Sem isso refresh, troca de organização e
  logout respondiam 401 em produção (spec `authentication`).
- **Origem do rate limit de login**: o nginx sobrescreve `X-Forwarded-For` com
  `$remote_addr` em vez de acrescentar ao valor do cliente — um header forjado
  deixava de contar para o IP real e anulava o bloqueio.
- **Storage de produção**: AWS S3 → OCI Object Storage (spec `file-storage`).

## Fora deste card (seguem abertos)

- `AuthRateLimiterRedisAdapter` loga `WARN "Rate limiter unavailable"` a cada
  login: `ttl()` do Quarkus lança `RedisKeyNotFoundException` para chave
  inexistente. O bloqueio funciona; o ruído mascara uma queda real do Redis.
- Dumps de backup ficam só no disco da VM — sem cópia externa.
- CI/CD: deploy segue manual (`git pull` + `compose up -d --build`).
