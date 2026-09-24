# Deploy de produção — OCI free tier

Runbook do deploy numa VM ARM (Ampere A1) da Oracle Cloud free tier, com Docker
e nginx. Uma unidade de deploy, uma VM. Todo merge na `main` roda os testes e,
se passarem, o GitHub Actions publica a nova versão por SSH (seção 9).

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

O cron roda como o seu usuário, não como root: o diretório dos dumps e o
arquivo de log precisam ser dele. Com um `sudo mkdir` simples os dois ficam do
root e o job falha toda noite sem avisar ninguém.

```bash
sudo install -d -o "$USER" -g "$USER" -m 750 /var/backups/lms
sudo install -o "$USER" -g "$USER" -m 640 /dev/null /var/log/lms-backup.log
./infra/scripts/backup-mysql.sh          # primeiro dump, na mão
crontab -e
```

```cron
0 3 * * * /opt/lms-nexus-jaas/infra/scripts/backup-mysql.sh >> /var/log/lms-backup.log 2>&1
```

A VM fica em UTC: `0 3` é meia-noite em Brasília.

Teste a restauração logo depois do primeiro dump — backup que nunca foi
restaurado é suposição, não backup. Faça o teste num banco descartável, não em
cima do `lms_db`: o `restore-mysql.sh` sobrescreve produção e para a API, e
qualquer escrita entre o dump e a restauração se perde.

```bash
set -a; . infra/.env; set +a
M="docker compose -f infra/docker-compose.prod.yml --env-file infra/.env exec -T -e MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql mysql --user=root"
$M -e "CREATE DATABASE lms_restore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
zcat /var/backups/lms/<arquivo>.sql.gz | $M lms_restore_test
$M -e "SELECT table_name FROM information_schema.tables WHERE table_schema='lms_restore_test'"
$M -e "DROP DATABASE lms_restore_test"
```

O `restore-mysql.sh` fica para a restauração de verdade, num incidente.

> Os dumps ficam no disco da própria VM: protegem de `down -v`, de migration
> ruim e de erro humano, **não** da perda da VM. Para isso, copie-os para fora
> (o bucket do Object Storage serve).

## 9. Atualizar

Todo push na `main` que mexa em `apps/`, `infra/` ou no próprio workflow dispara
`.github/workflows/deploy.yml`. Ele roda os testes da API (`mvn verify`) e do web
(lint, vitest e build). Se passarem, entra por SSH na VM e roda
`infra/scripts/deploy.sh`. O script faz `git pull --ff-only` e
`compose up -d --build`, espera a API ficar `healthy` e faz `image prune`. Os
deploys entram numa fila e nunca rodam dois ao mesmo tempo. O Flyway migra
sozinho quando a API sobe.

Para rodar sem push: aba **Actions → Deploy → Run workflow**, ou
`gh workflow run deploy.yml && gh run watch`. Para rodar direto na VM, sem
passar pelos testes:

```bash
/opt/lms-nexus-jaas/infra/scripts/deploy.sh
```

O `pull --ff-only` recusa sobrescrever mudanças locais em arquivos versionados.
Não edite o checkout da VM. `infra/.env` e `infra/keys` são gitignored e não
entram nessa regra.

### Configurar o deploy automático (uma vez)

**1. Chave dedicada**, gerada na sua máquina e usada só pelo Actions:

```bash
ssh-keygen -t ed25519 -N "" -C "github-actions-deploy" -f lms-deploy
```

**2. `authorized_keys` na VM.** O `command=` força essa chave a rodar só o
script, e `restrict` corta pty e forwarding. Se a chave vazar, ela só consegue
fazer um deploy da `main`:

```bash
echo "restrict,command=\"/opt/lms-nexus-jaas/infra/scripts/deploy.sh\" $(cat lms-deploy.pub)" \
  | ssh <usuario>@<ip-da-vm> 'cat >> ~/.ssh/authorized_keys'
```

O `git pull` roda sem ninguém digitando nada. Por isso a chave que a VM usa no
GitHub (a do `git clone` da seção 5) não pode ter passphrase.

**3. `known_hosts`.** Confira se a impressão digital do `ssh-keyscan` é a mesma
que a VM mostra. Sem essa checagem, fixar o host não protege nada:

```bash
ssh-keyscan -t ed25519 <ip-da-vm> > lms-known-hosts
ssh-keygen -lf lms-known-hosts                                   # na sua máquina
ssh <usuario>@<ip-da-vm> ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub   # na VM
```

**4. Environment e Secrets.** Os Secrets ficam no environment `production`.
Só o job de deploy os enxerga:

```bash
gh api -X PUT repos/AddisonSouza/lms-nexus-jaas/environments/production
gh secret set DEPLOY_HOST        --env production --body "<ip-da-vm>"
gh secret set DEPLOY_USER        --env production --body "<usuario>"
gh secret set DEPLOY_SSH_KEY     --env production < lms-deploy
gh secret set DEPLOY_KNOWN_HOSTS --env production < lms-known-hosts
shred -u lms-deploy          # a privada só precisa existir no GitHub
```

| Secret | Conteúdo |
|---|---|
| `DEPLOY_HOST` | IP público da VM |
| `DEPLOY_USER` | Usuário SSH da VM (o dono de `/opt/lms-nexus-jaas`, no grupo `docker`) |
| `DEPLOY_SSH_KEY` | Chave privada `lms-deploy`, inteira |
| `DEPLOY_KNOWN_HOSTS` | Linha do `ssh-keyscan` conferida no passo 3 |

**5. Atualizar o checkout da VM uma vez, à mão.** O `command=` do passo 2
aponta para o `deploy.sh` *que já está na VM*. Se o checkout for anterior ao
script, a chave do Actions não acha o que executar e o primeiro deploy falha
antes mesmo do `git pull`:

```bash
ssh <usuario>@<ip-da-vm> 'cd /opt/lms-nexus-jaas && git pull --ff-only && test -x infra/scripts/deploy.sh && echo ok'
```

Depois disso o próprio script faz o `git pull` a cada deploy.

**6. Testar:** `gh workflow run deploy.yml && gh run watch`.

## Acessar o banco

O MySQL publica a 3306 só no loopback da VM, então o acesso de fora é por
túnel SSH. No DBeaver (ou outro cliente):

- **SSH:** host `<IP da VM>`, porta 22, usuário `ubuntu`, chave privada da VM.
- **Main:** host `localhost`, porta `3306`, database `lms_db`, usuário `lms`,
  senha `MYSQL_PASSWORD` do `infra/.env`.
- **Driver properties:** `allowPublicKeyRetrieval=true` e `useSSL=false` (o
  tráfego já vai cifrado pelo SSH).

Marque a conexão como somente leitura. Schema muda só por migration do Flyway;
antes de alterar dado à mão, tire um dump (passo 8).

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
| `certbot renew` manual parece travado em "Processing ..." | Não está: sem TTY o certbot espera um atraso aleatório de até 8 min antes de renovar. O log mostra `random delay of N seconds` |
| Deploy falha com `Host key verification failed` | `DEPLOY_KNOWN_HOSTS` não bate com a chave atual da VM (VM recriada?). Refaça o passo 3 |
| Deploy falha no `git pull --ff-only` | Alguém mexeu em arquivo versionado na VM: `git status` lá, e descarte ou mova a mudança |
| Deploy falha esperando a API | O job mostra as últimas linhas do log da API. Se a subida for só lenta, aumente o default de `HEALTH_TIMEOUT` no `deploy.sh` |

```bash
# logs de tudo, seguindo
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env logs -f

# o que a API realmente recebeu de configuração
docker compose -f infra/docker-compose.prod.yml --env-file infra/.env exec api env | sort
```
