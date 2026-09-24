# chore: pipeline de deploy automático com GitHub Actions — Delta

## Added

- **Deploy contínuo** (`.github/workflows/deploy.yml`): todo push na `main` que
  toca `apps/**`, `infra/**` ou o próprio workflow roda `test-api` (`mvn verify`)
  e `test-web` (lint, vitest, build). Se os dois passam, o job `deploy` entra
  por SSH na VM. Também dispara manual (`workflow_dispatch`). Os deploys entram
  numa fila (`concurrency: deploy-prod`) e nunca rodam dois ao mesmo tempo.
- **`infra/scripts/deploy.sh`**, versionado e usável à mão: `git pull --ff-only`,
  `compose up -d --build`, espera a API ficar `healthy` (`HEALTH_TIMEOUT`, 300s)
  e faz `image prune`. Sai com erro e mostra o log da API se ela não subir.
- **Acesso do Actions à VM**: environment `production` com `DEPLOY_HOST`,
  `DEPLOY_USER`, `DEPLOY_SSH_KEY` e `DEPLOY_KNOWN_HOSTS` (host fixado, sem
  `StrictHostKeyChecking=no`). A chave é dedicada e, no `authorized_keys`, fica
  presa a `restrict,command=".../deploy.sh"`.
- **Runbook**: `docs/DEPLOY.md` §9 cobre o setup da chave e dos Secrets, o
  disparo manual e três linhas novas de diagnóstico.

## Changed

- **Testes de integração usam MySQL do Testcontainers** (#405): o datasource
  ficou restrito a `%dev`/`%prod`. Antes o `%test` herdava `localhost:3306` e
  os `*IT` rodavam contra o banco de dev.
- **CI gera um par JWT descartável** antes do `mvn verify` (#411). As chaves de
  dev são gitignored.
- **Atualizar produção**: `git pull` + `compose up` à mão → merge na `main`.

## Fora deste card (seguem abertos)

- ~~O `DEPLOY.md` não diz que, numa VM nova, é preciso um `git pull` antes do
  primeiro deploy automático.~~ Resolvido no #414 (passo 5 do setup).
- CI em PRs, branch protection, staging, rollback e alertas (fora de escopo).
