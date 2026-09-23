# Preparar deploy de produção na OCI free tier

## Objetivo

Habilitar o primeiro deploy em produção numa VM ARM (Ampere A1) da OCI free
tier, com Docker e nginx na frente. Hoje `infra/` só tem artefatos de dev: o
compose sobe a API com `mvn quarkus:dev` e o front com `vite dev`, publica as
portas de MySQL/Redis/MinIO no host e inclui Mailpit.

## Comportamento esperado

- A aplicação responde num **único domínio com HTTPS**: `/` serve a SPA e `/api`
  faz proxy para o Quarkus. Mesma origem elimina o CORS e viabiliza o cookie
  `Secure` + `SameSite=Strict` que o `AuthResource` já emite no perfil `%prod`.
- **Uploads** vão para o OCI Object Storage pela API S3-compatible, em bucket
  privado — o download já passa pela API (`S3StorageAdapter.retrieve`), então
  nada precisa ser exposto publicamente.
- **E-mails** de convite, confirmação de cadastro e reset de senha saem por SMTP
  real (Brevo/Resend), com os links apontando para o domínio de produção.
- As **chaves JWT** de produção são um par novo, gerado fora do repositório e
  montado como volume read-only — as antigas vazaram no histórico (`c9123de`).
- O primeiro usuário se cadastra pelo próprio fluxo (`POST /auth/register` →
  `POST /organizations` torna o criador `ADMIN_ORG`), então **não há seed**; o
  SMTP precisa estar funcionando antes do primeiro acesso, por causa da
  confirmação de e-mail.

## Fora de escopo

- Pipeline de CI/CD automatizado — o primeiro deploy é manual na VM.
- Mudanças no `docker-compose.yml` de desenvolvimento.
- Observabilidade (métricas, tracing, agregação de logs).
- Alta disponibilidade, réplicas ou autoscaling — é uma VM só.
