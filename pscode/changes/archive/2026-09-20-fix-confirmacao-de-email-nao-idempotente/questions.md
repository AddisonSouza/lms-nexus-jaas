# Questions

- [x] **Como tornar o 409 alcançável no backend?**
  → Não apagar o token; deixar o TTL de 24h expirá-lo. Remove `invalidate(token)`
  de `ConfirmEmailService`. Seguro: o token só ativa, a ativação é guardada pelo
  status, e `PENDING_CONFIRMATION` nunca é reatribuído depois do registro.

- [x] **O que a tela faz no 409?**
  → Trata como sucesso: card verde (CheckCircle) + redirect para
  `/login?confirmed=true` após 2s — mesmo caminho do 204.

- [x] **Link clicado após 24h com conta já ativa?**
  → Fora de escopo. Mantém 400 "Link inválido ou expirado" + botão de reenvio.
