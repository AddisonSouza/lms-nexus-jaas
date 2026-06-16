## ADDED Requirements

### Requirement: LocalStorageAdapter para desenvolvimento sem MinIO
O sistema SHALL fornecer uma implementação `LocalStorageAdapter` que armazena arquivos no filesystem local, selecionada automaticamente no profile `dev` sem necessidade de infraestrutura S3/MinIO.

#### Scenario: Upload de arquivo em ambiente de desenvolvimento
- **WHEN** use case chama `StoragePort.store(inputStream, filename, mimeType, context)` no profile `dev`
- **THEN** arquivo é salvo em `{project.root}/data/uploads/{context}/{ano}/{mes}/{uuid}-{filename}`; retorna `StoredFile` com `fileKey` equivalente ao caminho relativo

#### Scenario: Profile de produção não usa LocalStorageAdapter
- **WHEN** aplicação iniciada com profile `prod`
- **THEN** CDI injeta `S3StorageAdapter` — `LocalStorageAdapter` não é instanciado

#### Scenario: Serve arquivo salvo localmente
- **WHEN** usuário autenticado faz `GET /api/files/{fileKey}` e adaptador local está ativo
- **THEN** sistema lê o arquivo do filesystem local e retorna stream com `Content-Type` correto
