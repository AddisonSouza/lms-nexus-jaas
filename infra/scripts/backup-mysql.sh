#!/usr/bin/env bash
#
# Dump do MySQL de produção, com retenção. Pensado para rodar no cron da VM:
#
#   0 3 * * * /opt/lms-nexus-jaas/infra/scripts/backup-mysql.sh >> /var/log/lms-backup.log 2>&1
#
# O volume nomeado já sobrevive a `docker compose down` e a restart de
# container — o que ele não sobrevive é a um `down -v` distraído, à perda da VM
# ou a uma migration que apaga o que não devia. É desses casos que o dump
# protege.
#
# Variáveis (todas opcionais, com default):
#   BACKUP_DIR       onde gravar          (default /var/backups/lms)
#   BACKUP_KEEP_DAYS quantos dias manter  (default 7)
#
set -euo pipefail

cd "$(dirname "$0")/../.."

BACKUP_DIR="${BACKUP_DIR:-/var/backups/lms}"
BACKUP_KEEP_DAYS="${BACKUP_KEEP_DAYS:-7}"

if [ ! -f infra/.env ]; then
  echo "infra/.env não existe — nada a fazer." >&2
  exit 1
fi

# shellcheck disable=SC1091
set -a; . infra/.env; set +a

: "${MYSQL_ROOT_PASSWORD:?defina MYSQL_ROOT_PASSWORD em infra/.env}"
DB_NAME="${MYSQL_DATABASE:-lms_db}"

mkdir -p "$BACKUP_DIR"

STAMP="$(date +%Y-%m-%d_%H%M%S)"
TARGET="$BACKUP_DIR/${DB_NAME}_${STAMP}.sql.gz"
# Grava em .partial e só renomeia no fim: se o dump morrer no meio, sobra um
# arquivo com nome que a restauração não vai confundir com um backup bom.
PARTIAL="$TARGET.partial"

echo "==> Dump de $DB_NAME para $TARGET"

# A senha vai por variável de ambiente do processo, não na linha de comando:
# argumento apareceria para qualquer um que rodasse `ps` no container.
# --single-transaction dá um dump consistente sem travar escrita (InnoDB).
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env \
  exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
  mysqldump --user=root --single-transaction --quick --routines --triggers \
            --default-character-set=utf8mb4 "$DB_NAME" \
  | gzip -9 > "$PARTIAL"

mv "$PARTIAL" "$TARGET"
echo "==> Pronto: $(du -h "$TARGET" | cut -f1)"

echo "==> Removendo dumps com mais de $BACKUP_KEEP_DAYS dias"
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -type f -mtime "+$BACKUP_KEEP_DAYS" -print -delete
find "$BACKUP_DIR" -name "*.partial" -type f -mtime +1 -delete

echo "==> Backups atuais:"
ls -1sh "$BACKUP_DIR" | tail -n +2
