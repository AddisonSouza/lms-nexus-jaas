# Grill Me

- [x] Domínio próprio ou subdomínio gratuito? — Domínio próprio; o A record aponta para o IP público da VM.
- [x] Onde ficam os uploads em produção? — OCI Object Storage via API S3-compatible, bucket privado.
- [x] Qual SMTP real? — Brevo ou Resend, parametrizado por env var.
- [x] Onde as imagens Docker são construídas? — Na própria VM (`docker compose build`); a Ampere A1 tem folga e resolve arm64 naturalmente. Sem registry.
- [x] Como entram nginx e TLS? — Serviço nginx no compose + companion certbot com renovação automática e volume compartilhado dos certificados. A VM só precisa de Docker.
- [x] Como o `/api` chega no Quarkus? — O nginx remove o prefixo (`proxy_pass http://api:8080/`, com barra final). Zero mudança no app: dev e prod usam a mesma config e o `API_CONTRACT.md` segue literal. Só o front muda (`VITE_API_URL=/api`).
- [x] Backup do MySQL entra neste card? — Sim: script `mysqldump` em cron diário, com retenção de N dias.
