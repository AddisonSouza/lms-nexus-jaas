# file-storage Specification

## Purpose
Guardar e servir os arquivos do produto — anexos de tarefa, de submissão, de aviso e conteúdo de disciplina — atrás de uma porta que isola o domínio do S3/MinIO.
## Requirements
### Requirement: StoragePort abstrai armazenamento de arquivos
O módulo `storage` define `StoragePort` em `domain/port/out/`. Nenhuma referência a disco, S3 ou caminho físico no domínio ou use cases. `retrieve` devolve `RetrievedFile` (conteúdo + `StoredFile`), para a resposta HTTP ter tipo e nome sem uma segunda ida ao storage.

#### Scenario: Armazenar arquivo via S3StorageAdapter (MinIO em dev)
- **WHEN** Use case chama `StoragePort.store(inputStream, filename, mimeType, context)`
- **THEN** Arquivo enviado ao bucket S3/MinIO com key `{context}/{ano}/{mes}/{uuid}-{filename}` e o nome original no metadado `original-name`; retorna `StoredFile` com `fileKey`

#### Scenario: Trocar de MinIO (dev) para S3 real (prod) sem alterar use cases
- **WHEN** Variáveis de ambiente alteradas de endpoint MinIO para AWS S3 real
- **THEN** `S3StorageAdapter` passa a usar S3 sem qualquer alteração nos use cases — `StoragePort` permanece idêntico

---

### Requirement: Servir arquivo com autenticação
Endpoint `GET /files/{fileKey}` serve o arquivo a qualquer um dos quatro papéis, desde que autenticado. O prefixo `/api` foi removido para a rota seguir o padrão das demais (`/tasks`, `/classrooms`).

**Gap conhecido:** o endpoint não valida que o arquivo pertence à organização de quem pede — o `fileKey` não carrega `organization_id`. Card próprio.

#### Scenario: Acesso autorizado
- **WHEN** Usuário autenticado faz `GET /files/{fileKey}`
- **THEN** Resposta 200 com o stream do arquivo, `Content-Type` real gravado no upload e `Content-Disposition: attachment` com o nome original

#### Scenario: Nome original preservado no download
- **WHEN** O arquivo foi enviado como `prova final.pdf`
- **THEN** `Content-Disposition` traz `filename="prova_final.pdf"` (fallback ASCII) e `filename*=UTF-8''prova%20final.pdf` (RFC 5987), com o nome vindo do metadado `original-name` do objeto; objetos anteriores a esse metadado caem no nome embutido na chave

#### Scenario: Acesso não autenticado
- **WHEN** Request sem JWT válido
- **THEN** 401 Unauthorized

#### Scenario: Arquivo não encontrado
- **WHEN** `fileKey` não existe no storage
- **THEN** `NoSuchKeyException` do SDK vira `FileNotFoundException` do domínio e a resposta é 404 com `{"error":"FILE_NOT_FOUND"}` — antes subia crua como 500

---

### Requirement: Tipos de arquivo por contexto
`StorageContext.LESSON_MATERIAL` aceita: `pdf`, `mp4`, `webm`, `doc`, `docx`. `StorageContext.TASK_ATTACHMENT` aceita: `pdf`, `doc`, `docx`, `zip`, `jpg`, `png` (definido por configuração para facilitar extensão).

#### Scenario: Arquivo com extensão não permitida no contexto
- **WHEN** Upload com MIME type proibido para o contexto
- **THEN** `StoragePort.store()` lança `InvalidFileTypeException`; use case propaga como 422


### Requirement: LocalStorageAdapter para desenvolvimento sem MinIO
O sistema SHALL fornecer uma implementação `LocalStorageAdapter` que armazena arquivos no filesystem local, selecionada automaticamente no profile `dev` sem necessidade de infraestrutura S3/MinIO.

#### Scenario: Upload de arquivo em ambiente de desenvolvimento
- **WHEN** use case chama `StoragePort.store(inputStream, filename, mimeType, context)` no profile `dev`
- **THEN** arquivo é salvo em `{project.root}/data/uploads/{context}/{ano}/{mes}/{uuid}-{filename}`; retorna `StoredFile` com `fileKey` equivalente ao caminho relativo

#### Scenario: Profile de produção não usa LocalStorageAdapter
- **WHEN** aplicação iniciada com profile `prod`
- **THEN** CDI injeta `S3StorageAdapter` — `LocalStorageAdapter` não é instanciado

#### Scenario: Serve arquivo salvo localmente
- **WHEN** usuário autenticado faz `GET /files/{fileKey}` e adaptador local está ativo
- **THEN** sistema lê o arquivo do filesystem local e retorna stream com `Content-Type` correto
