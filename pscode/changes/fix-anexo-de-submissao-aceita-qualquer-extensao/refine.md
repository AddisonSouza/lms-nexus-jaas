# [fix] anexo de submissão aceita qualquer extensão

## Summary
Hoje o aluno consegue anexar qualquer arquivo à resposta de uma tarefa, até um
`.exe`. A correção passa a aceitar só PDF, DOC, DOCX, ZIP, JPG e PNG, recusando
o resto com uma mensagem clara na tela.

## Technical detail
- `SubmitTaskService` e `EditSubmissionService` chamam `storagePort.store(...)`
  sem validar tipo (contexto `SUBMISSION_ATTACHMENT`).
- Nova política de domínio `assessment/domain/model/AttachmentTypePolicy`: aceita
  só se a **extensão** (`.pdf .doc .docx .zip .jpg .jpeg .png`, case-insensitive)
  **e** o **MIME** declarado estiverem na lista; senão lança
  `InvalidAttachmentTypeException` (422, já existente no `assessment`).
- Validar **todos** os anexos antes do primeiro `store` → nada é gravado se
  algum for inválido.
- Não importar `curriculum.InvalidFileTypeException` (fronteira de módulos).
- Front: `submission.schema.ts` valida extensão além do MIME (`f.type` pode vir
  vazio); `SubmissionFormDialog` exibe o erro da mutation (hoje o 422 some).
  `accept` já existe.

## Scope
### In
- `POST /tasks/{id}/submissions` e `PUT /tasks/{id}/submissions/{submissionId}`.
- Testes unitários da política e dos dois serviços.
- Mensagem de erro no diálogo de envio do aluno.
### Out
- Anexo de tarefa do professor (`CreateTaskService`) e conteúdo de disciplina.
- Validação por magic bytes; antivírus; limite de tamanho.

## Subtasks
- [ ] Criar `AttachmentTypePolicy` (extensão + MIME) no domínio `assessment` com testes unitários
- [ ] Aplicar a política em `SubmitTaskService` e `EditSubmissionService` antes do `store`, com testes (`.exe` → 422, nada armazenado)
- [ ] Front: `submission.schema.ts` valida extensão além do MIME
- [ ] Front: `SubmissionFormDialog` exibe o erro 422 da submissão
