## ADDED Requirements

### Requirement: StoragePort abstrai armazenamento de arquivos
O módulo `storage` define `StoragePort` em `domain/port/out/`. Nenhuma referência a disco, S3 ou caminho físico no domínio ou use cases.

#### Scenario: Armazenar arquivo via S3StorageAdapter (MinIO em dev)
- **WHEN** Use case chama `StoragePort.store(inputStream, filename, mimeType, context)`
- **THEN** Arquivo enviado ao bucket S3/MinIO com key `{context}/{ano}/{mes}/{uuid}-{filename}`; retorna `StoredFile` com `fileKey`

#### Scenario: Trocar de MinIO (dev) para S3 real (prod) sem alterar use cases
- **WHEN** Variáveis de ambiente alteradas de endpoint MinIO para AWS S3 real
- **THEN** `S3StorageAdapter` passa a usar S3 sem qualquer alteração nos use cases — `StoragePort` permanece idêntico

---

### Requirement: Servir arquivo com autenticação
Endpoint `GET /api/files/{fileKey}` serve o arquivo autenticando o usuário e validando que pertence à organização dona do arquivo.

#### Scenario: Acesso autorizado
- **WHEN** Usuário autenticado faz `GET /api/files/{fileKey}` para arquivo da sua organização
- **THEN** Resposta 200 com stream do arquivo e `Content-Type` correto

#### Scenario: Acesso não autenticado
- **WHEN** Request sem JWT válido
- **THEN** 401 Unauthorized

#### Scenario: Arquivo não encontrado
- **WHEN** `fileKey` não existe no storage
- **THEN** 404 Not Found

---

### Requirement: Tipos de arquivo por contexto
`StorageContext.LESSON_MATERIAL` aceita: `pdf`, `mp4`, `webm`, `doc`, `docx`. `StorageContext.TASK_ATTACHMENT` aceita: `pdf`, `doc`, `docx`, `zip`, `jpg`, `png` (definido por configuração para facilitar extensão).

#### Scenario: Arquivo com extensão não permitida no contexto
- **WHEN** Upload com MIME type proibido para o contexto
- **THEN** `StoragePort.store()` lança `InvalidFileTypeException`; use case propaga como 422
