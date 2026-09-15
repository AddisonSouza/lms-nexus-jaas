# [fix] aluno não vê tarefas em Minhas Tarefas (schema my-grades)

## Summary
A página "Minhas Tarefas" do aluno fica vazia porque o front rejeita a resposta
da API por um campo que ela nunca envia. Corrige a validação, mostra erro de
carga quando algo falhar e adiciona o botão de editar a resposta antes do prazo.

## Technical detail
- `apps/web/src/features/assessment/api/submissions.ts`:
  `taskWithGradeSchema = taskSchema.extend(...)` herda `updatedAt:
  z.string().nullable()` (obrigatório). O DTO `TaskWithGradeResponse` não tem o
  campo → `ZodError` → `useQuery` em erro → página cai em `data = []`.
  Fix: derivar o schema com `taskSchema.omit({ updatedAt: true }).extend(...)`
  e ajustar o tipo `TaskWithGrade` em `types.ts`.
- `StudentTaskListPage.tsx` ignora `isError`. Usar `ListErrorState`
  (`@components/shared`) com `refetch`/`isFetching`, como em `SubjectListPage`.
- Edição: `useEditSubmission` e `updateSubmission` (PUT) já existem. Botão
  "Editar resposta" quando `submission.status === 'SUBMITTED'` e prazo aberto,
  reaproveitando `SubmissionFormDialog`. O summary de my-grades não traz
  `textResponse`, então o diálogo abre vazio com aviso de que substitui a
  resposta anterior.

## Scope
### In
- Schema/tipo de `my-grades` sem `updatedAt`.
- Estado de erro com retry na lista do aluno.
- Botão e fluxo "Editar resposta" (texto e/ou arquivos) antes do prazo.
- Testes: parse do schema com payload real; página em erro; botão de edição
  visível só com SUBMITTED + prazo aberto.
### Out
- Backend, contrato e spec de `GET /tasks/my-grades`.
- Pré-preencher o texto anterior na edição (exige `textResponse` no summary).
- Status `CLOSED` após prazo (#271).

## Subtasks
- [x] Ajustar `taskWithGradeSchema` e `TaskWithGrade` para não exigir `updatedAt`, com teste de parse usando payload real de my-grades
- [ ] `StudentTaskListPage` renderiza `ListErrorState` com retry quando `isError`, com teste
- [ ] Botão "Editar resposta" (SUBMITTED + prazo aberto) abrindo `SubmissionFormDialog` em modo edição via `useEditSubmission`, com teste
- [ ] Validar no navegador como ALUNO: lista aparece, envio, edição e "Ver Nota"
