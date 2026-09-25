# [fix] corrigir autorização do download de arquivos

## Summary
Hoje qualquer pessoa logada consegue baixar qualquer arquivo do sistema, inclusive de outra escola, desde que tenha o link. Com a correção, só baixa quem já pode ver aquele material, tarefa, entrega ou aviso nas telas.

## Technical detail
- `ServeFileUseCase` e `FileResource` hoje só exigem estar autenticado. A chave é `{contexto}/{ano}/{mes}/{uuid}-{nome}`.
- Novo port `storage/domain/port/out/FileAccessPort` com `supports(StorageContext)` e `canRead(fileKey, userId, orgId, role)`. `ServeFileUseCase` lê o contexto pelo prefixo da chave, chama o adapter correspondente (`Instance<FileAccessPort>`) e, se ele negar ou não houver adapter, lança `FileNotFoundException` → 404.
- `FileResource` passa `sub`, `org` e o papel do JWT para o use case.
- Adapters por módulo, reusando as regras atuais e buscando a linha por `file_key` + `organization_id`:
  - curriculum `LESSON_MATERIAL` (`subject_contents`): aluno só se `isMemberOfAnyClassroom` da disciplina
  - assessment `TASK_ATTACHMENT` (`task_attachments`): aluno só se a tarefa estiver `PUBLISHED`
  - assessment `SUBMISSION_ATTACHMENT` (`submission_attachments`): aluno dono ou professor criador da tarefa
  - communication `ANNOUNCEMENT_ATTACHMENT` (`announcement_attachments`): membro da turma
- As consultas ficam em `infrastructure/persistence/` (DB-07). Nenhuma migration.

## Scope
### In
- Port, dispatch e 404 no storage
- Um adapter por contexto nos 3 módulos donos
- ITs de permitido e negado por contexto
- Atualizar STG-03 no `DECISIONS.md` e a pendência na ADR-013
### Out
- Trocar o provedor de storage, URLs pré-assinadas, regras de upload
- Mudar endpoint, `API_CONTRACT.md` ou front

## Subtasks
- [x] storage: `FileAccessPort` + dispatch por contexto no `ServeFileUseCase` + JWT no `FileResource` (nega tudo sem adapter)
- [x] curriculum: adapter `LESSON_MATERIAL` + IT
- [x] assessment: adapter `TASK_ATTACHMENT` + IT
- [x] assessment: adapter `SUBMISSION_ATTACHMENT` + IT
- [x] communication: adapter `ANNOUNCEMENT_ATTACHMENT` + IT
- [ ] docs: STG-03 e ADR-013 sem a pendência
