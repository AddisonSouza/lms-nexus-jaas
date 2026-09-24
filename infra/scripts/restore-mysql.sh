#!/usr/bin/env bash
#
# Restaura um dump gerado pelo backup-mysql.sh. Um backup que nunca foi
# restaurado é uma suposição, não um backup — vale rodar isto uma vez logo
# depois do primeiro dump para saber que o caminho de volta funciona.
#
#   ./infra/scripts/restore-mysql.sh /var/backups/lms/lms_db_2026-09-23_030000.sql.gz
#
set -euo pipefail

cd "$(dirname "$0")/../.."

DUMP="${1:-}"
if [ -z "$DUMP" ] || [ ! -f "$DUMP" ]; then
  echo "uso: $0 <caminho-do-dump.sql.gz>" >&2
  exit 1
fi

# shellcheck disable=SC1091
set -a; . infra/.env; set +a

: "${MYSQL_ROOT_PASSWORD:?defina MYSQL_ROOT_PASSWORD em infra/.env}"
DB_NAME="${MYSQL_DATABASE:-lms_db}"

echo "Isto SOBRESCREVE o banco '$DB_NAME' em produção."
read -r -p "Digite o nome do banco para confirmar: " confirm
[ "$confirm" = "$DB_NAME" ] || { echo "Cancelado."; exit 1; }

# A API fica fora do ar durante a restauração para não escrever em cima.
echo "==> Parando a API"
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env stop api

echo "==> Restaurando $DUMP"
gunzip -c "$DUMP" | docker compose -f infra/docker-compose.prod.yml --env-file infra/.env \
  exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
  mysql --user=root --default-character-set=utf8mb4 "$DB_NAME"

echo "==> Subindo a API"
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env start api

echo "Pronto."
