# Questions — #77

- [x] Turma entra no escopo? → **Não.** RN-05 e RF-08 ficam intactos: o e-mail é a
  porta da organização, o código de 6 caracteres é a porta da turma.
- [x] O que resta para a organização (aceite pelo link já entregue em #130/#161)?
  → **Painel de convites** na página de membros: estado + reenviar + cancelar.
- [x] Reconvidar um e-mail com convite pendente? → **Substitui o anterior**: o
  pendente é cancelado e um novo token é emitido. Só um link vale por vez.
- [x] Como tratar "expirado"? → **Calculado na leitura** (`PENDING` com
  `expires_at` vencido). Sem job nem migração.
