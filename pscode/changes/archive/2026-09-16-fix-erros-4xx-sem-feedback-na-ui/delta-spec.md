# [fix] erros 4xx sem feedback na UI — Delta

## Added
- **Tradutor único de recusas da API** (`apps/web/src/lib/api-error.ts`).
  `apiErrorMessage(error, overrides?)` lê `errors[]` (sem o prefixo `campo:`),
  depois `error`, depois o fallback por status, depois um genérico. Distingue
  código (`TASK_FORBIDDEN`) de texto livre — parte das exceções da API devolve
  `getMessage()` como `error`. Prefixo de upload recusado (`File type not
  allowed: …`, em inglês com o MIME) vira "Este tipo de arquivo não é permitido.".
- **`ConfirmDialog` aceita `error`**: a recusa aparece com `role="alert"` acima
  do rodapé e o diálogo continua aberto. Usado ao excluir disciplina (403).
- **`AnnouncementForm` e `ContentFormDialog` aceitam `error`**: a recusa da
  mutation aparece acima do rodapé, junto dos erros do Zod. Cobrem publicar
  aviso (403) e anexar arquivo não permitido (422).
- **Limite de nota no cliente**: `createEvaluationSchema(maxScore)` recusa nota
  acima do máximo da tarefa com "Nota não pode exceder X".

## Changed
- **Reset de senha**: o 422 passa a listar os critérios que faltaram ("A senha
  precisa de: uma maiúscula, um número, um símbolo") em vez de "Erro ao
  redefinir senha. Tente novamente.". O texto do token gasto é preservado.
- **Aceite de convite**: convite endereçado a outro e-mail (403) passa a dizer
  "Este convite foi enviado para outro e-mail. Entre com a conta convidada."
  em vez de "Tente novamente.". Os mapas inline de status saem em favor do
  helper, sem mudar os textos de 409 e 410.
- **Campo de nota da avaliação**: perde `min`/`max` nativos. O balão do
  navegador vinha em inglês e barrava o envio antes do Zod rodar; os limites
  agora são do schema, em português.

## Removed
- Mapas de `response.status` inline em `ResetPasswordPage` e `AcceptInvitePage`.
