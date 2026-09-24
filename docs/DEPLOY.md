# Deploy de produção — OCI free tier

Runbook do deploy numa VM ARM (Ampere A1) da Oracle Cloud free tier, com Docker
e nginx. Uma unidade de deploy, uma VM, sem CI/CD: o deploy é `git pull` +
`docker compose up -d --build`.

## Topologia

```
                 :443 ─┐
Internet ──────────────┤  nginx (serviço web)  ──/api/──▶  api    (Quarkus :8080)
                 :80 ──┘   TLS + SPA + proxy       │
                                                   ├──▶  mysql  (volume)
                                                   └──▶  redis  (AOF, volume)
                           certbot ──▶ renova o certificado
```

Um domínio só. `/` serve a SPA, `/api/` cai no Quarkus com o prefixo removido
pela barra final do `proxy_pass`. Mesma origem: o CORS deixa de existir e o
cookie de sessão (`Secure` + `SameSite=Strict`) funciona.

**Nada além do nginx é exposto.** MySQL, Redis e a API não publicam porta no
host; só se enxergam pela rede interna do compose.

## 1. Provisionar a VM

No console da OCI: **Compute → Instances → Create**.

- **Shape:** `VM.Standard.A1.Flex`, **2 OCPU / 12 GB** — a cota Always Free
  inteira. O shape AMD micro (1 GB) **não** serve — não segura Quarkus +
  MySQL + Redis.
- **Imagem:** Ubuntu 22.04 (aarch64).
- **Boot volume:** 50 GB bastam; o Always Free dá 200 GB no total.
- **Região:** a home region da tenancy. Recurso Always Free criado fora dela
  é cobrado.

> **Não peça 4 OCPU / 24 GB.** Era a cota até 15/06/2026, quando a Oracle a
> cortou pela metade sem anúncio — só editando a documentação. Desde
> 18/08/2026 o limite é aplicado: instância acima da cota é **terminada
> automaticamente** (ou cobrada, em conta Pay-As-You-Go). O selo
> `Always Free-eligible` no console é do *shape*, não da quantidade de OCPU —
> ele aparece em 4 OCPU do mesmo jeito.

> `Out of capacity` em ARM é comum e não é erro de configuração. Onde a região
> tem mais de um Availability Domain, tente outro; em região de AD único —
> como Vinhedo (`sa-vinhedo-1`) — só resta repetir mais tarde.

Guarde o **IP público** e a chave SSH.

## 2. Abrir as portas — nos dois lugares

Esquecer o segundo é o erro clássico: a Security List libera e o firewall da
instância continua descartando o pacote, sem log e sem mensagem.

**a) Security List da VCN** (console: *Networking → VCN → Subnet → Security
List → Add Ingress Rules*): TCP 80 e 443, origem `0.0.0.0/0`.

**b) Firewall da instância** (as imagens Ubuntu da OCI vêm com regras que
rejeitam tudo fora do SSH). As regras novas precisam entrar **antes** do
`REJECT` final — o que vier depois dele nunca é avaliado. Confira a posição:

```bash
sudo iptables -L INPUT -n --line-numbers   # na imagem 22.04 o REJECT é a linha 5
```

Insira na linha do `REJECT` (ele desce para depois delas):

```bash
sudo iptables -I INPUT 5 -p tcp -m state --state NEW --dport 443 -j ACCEPT
sudo iptables -I INPUT 5 -p tcp -m state --state NEW --dport  80 -j ACCEPT
sudo netfilter-persistent save        # sem isto as regras somem no reboot
```

Faça isso **antes** de instalar o Docker: um `netfilter-persistent save` com o
Docker rodando grava também as chains dele em `/etc/iptables/rules.v4`.

Para testar de fora, com o Docker já instalado (seção 4) e antes do nginx de
verdade existir: `curl http://<ip>` com um `docker run --rm -p 80:80 nginx:alpine` na VM deve
dar 200. Na 443, `Connection refused` é bom sinal (o pacote chegou e ninguém
escuta); `No route to host` é o `REJECT` ainda na frente.

## 3. DNS

Aponte um registro **A** do domínio para o IP público e confirme antes de
seguir — o certbot falha se o nome ainda não resolver:

```bash
dig +short lms.seudominio.com.br
```

**`www`:** se `www.<domínio>` resolver (um CNAME para o domínio raiz basta), o
`init-letsencrypt.sh` o inclui no certificado sozinho e o nginx o redireciona
com 301 para o domínio raiz. Se não resolver, fica de fora sem quebrar nada. Não
deixe o `www` apontando para a VM sem estar no certificado: o HSTS sai com
`includeSubDomains`, e o navegador bloqueia o `www` sem opção de prosseguir.

## 4. Docker na VM

```bash
sudo apt-get update && sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo tee /etc/apt/keyrings/docker.asc > /dev/null
echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER" && newgrp docker
```

## 5. Código e configuração

```bash
sudo mkdir -p /opt && sudo chown "$USER" /opt
git clone git@github.com:AddisonSouza/lms-nexus-jaas.git /opt/lms-nexus-jaas
cd /opt/lms-nexus-jaas

cp infra/.env.prod.example infra/.env
$EDITOR infra/.env          # preencha tudo; o compose recusa subir com campo vazio
```

Gere o par de chaves JWT **na VM** — as do repositório vazaram no histórico
(`c9123de`) e não podem ser usadas:

```bash
mkdir -p infra/keys && cd infra/keys
openssl genrsa -out private.pem 2048
openssl pkcs8 -topk8 -inform PEM -in private.pem -out privateKey.pem -nocrypt
openssl rsa -in private.pem -pubout -out publicKey.pem
rm private.pem
# A API roda como UID 1001 no container (infra/docker/api/Dockerfile); o
# usuário da VM é outro UID. Com o dono errado a API não lê a chave e morre na
# subida — o diretório é montado read-only, então o dono tem que ser o 1001.
sudo chown 1001:1001 privateKey.pem publicKey.pem
sudo chmod 400 privateKey.pem && sudo chmod 444 publicKey.pem
cd /opt/lms-nexus-jaas
```

## 6. Primeira subida

O build roda na própria VM e leva alguns minutos na primeira vez (o Maven baixa
todas as dependências):

```bash
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env build
```

Emita o certificado. O script resolve o ovo-e-galinha entre nginx e certbot
subindo com um certificado descartável, trocando pelo real e recarregando:

```bash
./infra/nginx/init-letsencrypt.sh
```

Com `CERTBOT_STAGING=1` no `.env` ele usa o ambiente de teste do Let's Encrypt —
o navegador vai reclamar do certificado, e está certo. Quando o fluxo rodar
limpo, comente `CERTBOT_STAGING`, apague o volume e repita:

```bash
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env down
docker volume rm lms_certbot_conf
./infra/nginx/init-letsencrypt.sh
```

Suba tudo:

```bash
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env up -d
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env ps
```

## 7. Primeiro usuário

**Não existe seed.** O primeiro administrador nasce pelo fluxo normal, e ele
depende do e-mail funcionando:

1. `https://<domínio>/register` — cadastro.
2. Confirme pelo link que chega no e-mail. **Se o SMTP não estiver funcionando,
   o cadastro trava aqui e ninguém entra.**
3. Faça login e crie a organização: quem cria vira `ADMIN_ORG`.
4. Convide os demais pela tela de membros.

Antes de tudo, confirme que a API está viva:

```bash
curl -fsS https://<domínio>/api/q/health/ready
```

## 8. Backup

```bash
sudo mkdir -p /var/backups/lms
crontab -e
```

```cron
0 3 * * * /opt/lms-nexus-jaas/infra/scripts/backup-mysql.sh >> /var/log/lms-backup.log 2>&1
```

Rode uma restauração de teste logo depois do primeiro dump — backup que nunca
foi restaurado é suposição, não backup:

```bash
./infra/scripts/restore-mysql.sh /var/backups/lms/<arquivo>.sql.gz
```

## 9. Atualizar

```bash
cd /opt/lms-nexus-jaas && git pull
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env up -d --build
docker image prune -f
```

O Flyway migra sozinho na subida da API.

## Diagnóstico

| Sintoma | Onde olhar |
|---|---|
| `502 Bad Gateway` | A API ainda está subindo ou morreu: `docker compose ... logs api` |
| Login responde 200 mas não autentica | Cookie `Secure` exige HTTPS de verdade — confira se o certificado é válido |
| Todo mundo bloqueado no login | `proxy-address-forwarding`: sem ele o rate limit vê só o IP do nginx |
| Convite/reset com link `localhost` | `APP_BASE_URL` / `PASSWORD_RESET_URL` não chegaram no container |
| Upload falha com 413 | `client_max_body_size` do nginx abaixo do limite da API (50 MiB) |
| Upload falha com erro de bucket | `STORAGE_ENDPOINT` errado, ou credencial de API no lugar da Customer Secret Key |
| Certificado não renova | `docker compose ... logs certbot`; a 80 precisa estar aberta nos dois firewalls |

```bash
# logs de tudo, seguindo
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env logs -f

# o que a API realmente recebeu de configuração
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env exec api env | sort
```
