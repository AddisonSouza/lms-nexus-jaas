# ADR-012 — Deploy em VM Única na OCI com Docker Compose

**Status:** Aceito  
**Data:** Setembro 2026

## Contexto
O projeto precisava de um ambiente de produção público, com HTTPS, a custo zero e operável por uma pessoa. Alternativas: PaaS gerenciado (Render, Fly.io), Kubernetes, ou VM própria.

## Decisão
Uma VM ARM (Ampere A1) no free tier da Oracle Cloud (`sa-vinhedo-1`) rodando `infra/docker-compose.prod.yml`:
- **nginx** como borda: termina TLS (Let's Encrypt via certbot), serve a SPA e faz proxy de `/api` para o Quarkus
- **API, MySQL e Redis** em rede interna do compose; só 80/443 publicadas (MySQL apenas no loopback, para túnel SSH)
- **Deploy contínuo** pelo GitHub Actions (`deploy.yml`): push na `main` → testes da API e do web → SSH na VM → `infra/scripts/deploy.sh`
- **Backup** do MySQL por cron (`infra/scripts/backup-mysql.sh`)

Runbook completo em `docs/DEPLOY.md`.

## Justificativa
- Custo zero com recursos suficientes (`VM.Standard.A1.Flex`, 2 OCPU / 12 GB ARM) para o monolito (ADR-008)
- O mesmo Compose do desenvolvimento, com poucas diferenças — sem nova ferramenta para aprender
- Mesma origem para SPA e API simplifica autenticação por cookie (ADR-011)
- PaaS gratuitos têm hibernação e limites que prejudicariam a demonstração do TCC

## Consequências
- Ponto único de falha: sem alta disponibilidade nem escala horizontal
- Build das imagens acontece na própria VM (`compose up --build`); deploys são serializados (`concurrency: deploy-prod`)
- Segredos ficam em `infra/.env` na VM e nos Secrets do environment `production` do GitHub
- Migrar para orquestrador no futuro não exige mudança de código — só de infraestrutura
