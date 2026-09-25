# [fix] corrigir autorização do download de arquivos — Delta

## Added
- **`FileAccessPort`** (`storage/domain/port/out`): regra de leitura por
  `StorageContext`, implementada pelo módulo dono — `LessonMaterialAccessService`
  (curriculum), `TaskAttachmentAccessService` e `SubmissionAttachmentAccessService`
  (assessment), `AnnouncementAttachmentAccessService` (communication).
- `StorageContext.fromFileKey` resolve o contexto pelo prefixo da chave.
- Consultas por `file_key` + `organization_id` nos repositórios de conteúdo,
  tarefa, submissão e aviso (ignoram registros com soft delete).

## Changed
- `GET /files/{fileKey}`: qualquer usuário autenticado → só quem enxerga o
  recurso dono, na organização do JWT, com a mesma regra da tela que o lista.
- 404 `FILE_NOT_FOUND` passa a cobrir também "sem acesso" e "chave sem dono",
  para não confirmar que o arquivo existe. Padrão fechado: contexto sem regra ou
  JWT sem organização negam.
- `ADMIN_ORG`/`GESTOR` não baixam anexos de submissão (não os veem nas telas).
- STG-03, ADR-013 e `API_CONTRACT.md` documentam as regras.

## Removed
- Gap "o endpoint não valida a organização" (spec `file-storage`, contrato).
- Requirement `LocalStorageAdapter` da spec `file-storage` — nunca
  implementado (ADR-013).
