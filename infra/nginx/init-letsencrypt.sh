#!/usr/bin/env bash
#
# Emite o certificado inicial do Let's Encrypt. Roda uma vez, na VM, depois que
# o A record do domínio já aponta para o IP público.
#
# Existe um ovo-e-galinha aqui: o nginx não sobe sem os arquivos de certificado
# que a config referencia, e o certbot não emite o certificado sem um nginx
# respondendo o desafio na porta 80. A saída é subir com um certificado
# auto-assinado descartável, deixar o certbot trocá-lo pelo de verdade e
# recarregar. A renovação depois é automática, pelo serviço `certbot`.
#
#   ./infra/nginx/init-letsencrypt.sh
#
set -euo pipefail

cd "$(dirname "$0")/../.."

COMPOSE="docker compose -f infra/docker-compose.prod.yml --env-file infra/.env"

if [ ! -f infra/.env ]; then
  echo "infra/.env não existe — copie de infra/.env.prod.example e preencha." >&2
  exit 1
fi

# shellcheck disable=SC1091
set -a; . infra/.env; set +a

: "${DOMAIN:?defina DOMAIN em infra/.env}"
: "${CERTBOT_EMAIL:?defina CERTBOT_EMAIL em infra/.env}"

CERT_PATH="/etc/letsencrypt/live/$DOMAIN"

if $COMPOSE run --rm --entrypoint "test -f $CERT_PATH/fullchain.pem" certbot 2>/dev/null; then
  echo "Certificado para $DOMAIN já existe. Nada a fazer."
  echo "Para forçar a reemissão, remova o volume: docker volume rm lms_certbot_conf"
  exit 0
fi

echo "==> Gerando certificado auto-assinado temporário para $DOMAIN"
$COMPOSE run --rm --entrypoint sh certbot -c "
  mkdir -p '$CERT_PATH' &&
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout '$CERT_PATH/privkey.pem' \
    -out '$CERT_PATH/fullchain.pem' \
    -subj '/CN=$DOMAIN'"

echo "==> Subindo o nginx para responder o desafio do ACME"
$COMPOSE up -d web

echo "==> Removendo o certificado temporário"
$COMPOSE run --rm --entrypoint sh certbot -c "rm -rf '$CERT_PATH' /etc/letsencrypt/archive/$DOMAIN /etc/letsencrypt/renewal/$DOMAIN.conf"

echo "==> Pedindo o certificado ao Let's Encrypt"
# --staging enquanto testa: o limite de emissões por domínio é de 5 por semana e
# queimá-lo custa dias de espera. Tire a flag quando o fluxo estiver redondo.
$COMPOSE run --rm certbot certonly \
  --webroot --webroot-path /var/www/certbot \
  --email "$CERTBOT_EMAIL" \
  --agree-tos --no-eff-email \
  ${CERTBOT_STAGING:+--staging} \
  -d "$DOMAIN"

echo "==> Recarregando o nginx com o certificado definitivo"
$COMPOSE exec web nginx -s reload

echo "Pronto. https://$DOMAIN"
