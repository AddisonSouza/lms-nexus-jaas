# [fix] erros 4xx sem feedback na UI

## Summary
Quando a API recusa uma ação (sem permissão, arquivo não permitido, senha
fraca, convite de outro e-mail), a tela hoje não diz nada ou diz só "tente
novamente". Passa a mostrar a mensagem certa em cada formulário, com um único
tradutor de erros reaproveitado por todas as telas.

## Technical detail
- API devolve dois formatos: `{ "error": "CODE" }` (regra de negócio, ex.
  `TASK_FORBIDDEN`, `INVITATION_NOT_FOR_THIS_USER`, `CLASSROOM_ARCHIVED`) e
  `{ "errors": ["campo: mensagem"] }` (validação 422). Não há helper: cada
  página mapeia `response.status` inline (`ResetPasswordPage`,
  `AcceptInvitePage`, `TaskListPage.publishErrorMessage`).
- Novo `apps/web/src/lib/api-error.ts`: `apiErrorMessage(error, overrides?)`
  lê `errors[]` (junta as mensagens sem o prefixo `campo:`), depois `error`
  (mapa código→PT-BR), depois fallback por status (403 "Você não tem permissão
  para esta ação", 422 "Dados inválidos", genérico). Zod para a resposta.
- `ConfirmDialog` (`components/shared`) ganha `error?: string | null` e não
  fecha no `onError`; usado em 7 telas, só as afetadas passam a prop.
- Diálogos de formulário (`AnnouncementForm`, `ContentFormDialog`,
  `EvaluationDialog`) recebem `error?: string | null` e renderizam acima do
  rodapé, como já fazem com erros do Zod.
- Nota acima do máximo é validação de cliente: `evaluationSchema` recebe
  `maxScore` (`.max(maxScore, ...)`) via `superRefine`/factory.

## Scope
### In
- Helper `apiErrorMessage` + testes unitários (formatos `error`, `errors`,
  status sem corpo).
- `ConfirmDialog` com erro: excluir disciplina (`SubjectListPage`).
- Aviso (`AnnouncementFeed`/`AnnouncementForm`), conteúdo
  (`SubjectDetailPage`/`ContentFormDialog`), reset de senha, aceite de convite
  (403 e demais códigos), avaliação (nota > máximo no cliente).
- Migrar os mapas inline existentes para o helper sem mudar os textos atuais.
### Out
- Toasts globais; interceptor Axios; esconder ações por papel (#275);
  backend.

## Subtasks
- [x] `lib/api-error.ts` com `apiErrorMessage` e testes (error, errors, status, desconhecido)
- [x] `ConfirmDialog` com prop `error` + excluir disciplina exibindo 403, com teste
- [x] `AnnouncementForm` e `ContentFormDialog` exibindo erro da mutation (403/422), com testes
- [x] `ResetPasswordPage` e `AcceptInvitePage` migrados para o helper (422 lista critérios; 403 e-mail divergente), com testes
- [x] `evaluationSchema` com limite `maxScore` e mensagem "Nota não pode exceder X", com teste
- [ ] Validar no navegador os seis casos da bateria com os perfis correspondentes
