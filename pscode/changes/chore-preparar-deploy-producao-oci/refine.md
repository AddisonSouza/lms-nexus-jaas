# Preparar deploy de produção na OCI free tier

## Summary

Hoje só existe infraestrutura de desenvolvimento: o compose sobe a API com
`mvn quarkus:dev`, o front com `vite dev`, publica MySQL/Redis/MinIO no host e
inclui Mailpit. Este card cria os artefatos que faltam para a aplicação rodar
numa VM ARM (Ampere A1) da OCI free tier, atrás de um nginx com HTTPS, num
único domínio.

## Technical detail

- **Origem única.** O nginx serve a SPA em `/` e faz `proxy_pass
  http://api:8080/` (barra final) em `/api/`, removendo o prefixo. Nenhum
  `@Path` muda e o `API_CONTRACT.md` segue literal; o front passa a usar
  `VITE_API_URL=/api`, que o `axios.ts` consome como `baseURL` relativo.
- **TLS é pré-requisito, não acabamento.** `AuthResource.java:91` emite o cookie
  com `secure(true)` + `SameSite=STRICT` no perfil `%prod` — sem HTTPS o login
  não funciona. Certificado via certbot, renovado automaticamente.
- **Config com default `localhost`.** `lms.app.base-url` (usado em
  `InvitationMailService` e `QuarkusMailAdapter`) **nem está declarado** no
  `application.properties` — só existe como `defaultValue`. Idem
  `lms.auth.password-reset.url`, `CORS_ORIGINS` e `JWT_ISSUER`. Sem isso,
  convite e reset de senha saem com link para `localhost:5173`.
- **Storage.** O perfil `%prod` não tem `quarkus.s3.endpoint-override`, então o
  SDK bateria na AWS real. Apontar para o endpoint S3-compatible da OCI
  (`https://<namespace>.compat.objectstorage.<region>.oraclecloud.com`) com
  `path-style-access=true` e Customer Secret Keys. O bucket fica privado — o
  download já passa pela API (`S3StorageAdapter.retrieve`).
- **Chaves JWT.** Par novo, gerado na VM fora do repositório e montado
  read-only; `JWT_PRIVATE_KEY_PATH`/`JWT_PUBLIC_KEY_PATH` já são lidos no
  `%prod`. As antigas vazaram no histórico (`c9123de`).
- **Sem seed.** O primeiro usuário nasce pelo fluxo normal (`/auth/register` →
  `POST /organizations` torna o criador `ADMIN_ORG`), que exige confirmação de
  e-mail — logo o SMTP precisa funcionar **antes** do primeiro acesso.
- **Healthcheck.** `quarkus-smallrye-health` não está no `pom.xml`; sem ele o
  compose não sabe quando a API subiu. A imagem JRE slim não tem `curl`.

## Scope

### In

- Dockerfiles de produção da API e do web (arm64).
- `infra/docker-compose.prod.yml`, `infra/nginx/` e certbot.
- Perfil `%prod` do `application.properties` + `quarkus-smallrye-health`.
- Backup `mysqldump` em cron com retenção.
- `.env.prod.example` e runbook de deploy.

### Out

- Pipeline de CI/CD — o primeiro deploy é manual na VM.
- `infra/docker-compose.yml` de desenvolvimento permanece intocado.
- Observabilidade (métricas, tracing, agregação de logs).
- Alta disponibilidade, réplicas ou autoscaling.

## Subtasks

- [ ] Adicionar `quarkus-smallrye-health` ao `pom.xml` e completar o perfil `%prod` do `application.properties` (storage OCI, base URLs, CORS, issuer)
- [ ] Dockerfile de produção da API: multi-stage Maven → fast-jar em JRE 21 slim, com `.dockerignore`
- [ ] Dockerfile de produção do web: build Vite → nginx servindo `dist/` com fallback de SPA
- [ ] `infra/nginx/`: vhost do domínio único (`/` SPA, `/api/` → Quarkus) com bloco ACME e redirect 80→443
- [ ] `infra/docker-compose.prod.yml`: api, web, mysql, redis, nginx e certbot, sem publicar portas internas, com healthchecks e `restart: unless-stopped`
- [ ] Script de backup `mysqldump` com retenção + entrada de cron
- [ ] `.env.prod.example` e `docs/DEPLOY.md` com o runbook (VM, Security List + firewall, chaves JWT, primeiro `up`, emissão do certificado)
