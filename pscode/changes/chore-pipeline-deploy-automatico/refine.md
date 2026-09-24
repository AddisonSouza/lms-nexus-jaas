# [chore] pipeline de deploy automático com GitHub Actions

## Summary
Todo merge na `main` que mexa em código ou infra roda os testes e, se passarem,
publica a nova versão em produção sozinho. Ninguém mais precisa entrar no
servidor para atualizar a aplicação.

## Technical detail
- Workflow novo em `.github/workflows/deploy.yml`, disparado por `push` na `main` com `paths: apps/**, infra/**, .github/workflows/deploy.yml` e por `workflow_dispatch`.
- `concurrency: deploy-prod` com `cancel-in-progress: false`: deploys enfileiram, nunca rodam dois ao mesmo tempo na VM.
- Job `test-api`: Temurin 21 + cache Maven, `mvn -B verify` em `apps/api`. O Docker do `ubuntu-latest` serve o Testcontainers.
- Job `test-web`: Node 20 (o mesmo do Dockerfile) + cache npm, e em `apps/web` roda `npm ci`, `lint`, `vitest run` e `build`.
- Job `deploy` (`needs` dos dois): SSH com chave dedicada e `known_hosts` fixado, ambos vindos de Secrets (`DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`, `DEPLOY_KNOWN_HOSTS`). Ele chama `infra/scripts/deploy.sh` na VM.
- O `deploy.sh` fica versionado e também roda à mão: `git pull --ff-only`, `compose up -d --build`, espera o `api` ficar `healthy` (com timeout) e faz `image prune -f`. Sai com erro se o health falhar.
- O `pull --ff-only` falha, em vez de sobrescrever, se alguém tiver mexido no checkout da VM. `infra/.env` e `infra/keys` são gitignored e não são afetados.
- `environment: production` no job de deploy agrupa os Secrets e mostra o histórico de deploys no GitHub.

## Scope
### In
- Workflow com testes bloqueando o deploy.
- Script de deploy na VM com espera pelo health check.
- Runbook (`docs/DEPLOY.md`): chave dedicada, `authorized_keys`, lista de Secrets, disparo manual. A seção "Atualizar" passa a apontar para o script.
### Out
- CI em PRs, branch protection, staging, rollback, registry/GHCR, alertas.



## Subtasks

- [x] Criar `infra/scripts/deploy.sh` (pull ff-only, compose up --build, espera health, prune)
- [x] Criar `.github/workflows/deploy.yml` com triggers, concurrency e os jobs `test-api` e `test-web`
- [x] Adicionar o job `deploy` (SSH via Secrets + `environment: production`) chamando o script
- [ ] Atualizar `docs/DEPLOY.md`: setup da chave/Secrets, disparo manual e seção "Atualizar"
