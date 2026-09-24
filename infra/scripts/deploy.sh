#!/usr/bin/env bash
#
# Atualiza a produção para o que está na main. Quem chama é o job `deploy` do
# GitHub Actions (via SSH), mas o script também roda à mão na VM:
#
#   /opt/lms-nexus-jaas/infra/scripts/deploy.sh
#
# O `pull --ff-only` falha, em vez de sobrescrever, se alguém tiver commitado ou
# mexido em arquivo versionado no checkout da VM. `infra/.env` e `infra/keys`
# são gitignored e não são tocados.
#
# Sai com erro se a API não ficar `healthy` a tempo — o job do Actions fica
# vermelho e o log mostra o motivo.
#
# Variáveis (opcionais, com default):
#   HEALTH_TIMEOUT  segundos esperando a API ficar healthy (default 300)
#
set -euo pipefail

cd "$(dirname "$0")/../.."

HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-300}"

if [ ! -f infra/.env ]; then
  echo "infra/.env não existe — nada a fazer." >&2
  exit 1
fi

compose() {
  docker compose -f infra/docker-compose.prod.yml --env-file infra/.env "$@"
}

echo "==> git pull --ff-only"
git pull --ff-only
echo "    em $(git rev-parse --short HEAD): $(git log -1 --format=%s)"

echo "==> compose up -d --build"
compose up -d --build

# O healthcheck da API tem start_period de 90s e até 10 falhas a cada 15s, então
# o Docker leva até ~240s para desistir. O timeout padrão fica acima disso.
echo "==> esperando a API ficar healthy (timeout ${HEALTH_TIMEOUT}s)"
API_ID="$(compose ps -q api)"
if [ -z "$API_ID" ]; then
  echo "container da API não encontrado." >&2
  exit 1
fi

deadline=$(( $(date +%s) + HEALTH_TIMEOUT ))
while :; do
  status="$(docker inspect -f '{{.State.Health.Status}}' "$API_ID")"
  case "$status" in
    healthy)
      echo "    API healthy."
      break
      ;;
    unhealthy)
      echo "API ficou unhealthy. Últimas linhas do log:" >&2
      compose logs --tail 50 api >&2
      exit 1
      ;;
  esac
  if [ "$(date +%s)" -ge "$deadline" ]; then
    echo "API não ficou healthy em ${HEALTH_TIMEOUT}s (status: $status). Últimas linhas do log:" >&2
    compose logs --tail 50 api >&2
    exit 1
  fi
  sleep 5
done

echo "==> image prune -f"
docker image prune -f

echo "==> deploy concluído."
