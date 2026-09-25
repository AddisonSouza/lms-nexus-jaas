# file-storage Specification

## Purpose
Guardar e servir os arquivos do produto — anexos de tarefa, de submissão, de aviso e conteúdo de disciplina — atrás de uma porta que isola o domínio do S3/MinIO.
## Requirements
### Requirement: StoragePort abstrai armazenamento de arquivos
O módulo `storage` define `StoragePort` em `domain/port/out/`. Nenhuma referência a disco, S3 ou caminho físico no domínio ou use cases. `retrieve` devolve `RetrievedFile` (conteúdo + `StoredFile`), para a resposta HTTP ter tipo e nome sem uma segunda ida ao storage.

#### Scenario: Armazenar arquivo via S3StorageAdapter (MinIO em dev)
- **WHEN** Use case chama `StoragePort.store(inputStream, filename, mimeType, context)`
- **THEN** Arquivo enviado ao bucket S3/MinIO com key `{context}/{ano}/{mes}/{uuid}-{filename}` e o nome original no metadado `original-name`; retorna `StoredFile` com `fileKey`

#### Scenario: Trocar de MinIO (dev) para OCI Object Storage (prod) sem alterar use cases
- **WHEN** O perfil `%prod` aponta `quarkus.s3.endpoint-override` para o endpoint S3-compatible da OCI (`https://<namespace>.compat.objectstorage.<region>.oraclecloud.com`), com `path-style-access=true` e uma Customer Secret Key como credencial
- **THEN** `S3StorageAdapter` grava e lê no bucket da OCI sem qualquer alteração nos use cases — `StoragePort` permanece idêntico; o bucket é privado e o download continua passando por `GET /files/{fileKey}`

---

### Requirement: Servir arquivo a quem enxerga o recurso dono
Endpoint `GET /files/{fileKey}` (qualquer um dos quatro papéis, autenticado) só serve o arquivo a quem enxerga o recurso que o referencia, na organização do JWT. O `ServeFileUseCase` lê o `StorageContext` pelo prefixo da chave e consulta o `FileAccessPort` do módulo dono, que aplica a mesma regra da tela que lista o recurso:

| Contexto | Módulo | Quem lê |
|---|---|---|
| `LESSON_MATERIAL` | curriculum | qualquer papel da org; `ALUNO` só se membro de turma vinculada à disciplina |
| `TASK_ATTACHMENT` | assessment | qualquer papel da org; `ALUNO` só se a tarefa está `PUBLISHED`/`CLOSED` |
| `SUBMISSION_ATTACHMENT` | assessment | o aluno autor e o professor que criou a tarefa |
| `ANNOUNCEMENT_ATTACHMENT` | communication | membros da turma do aviso |

Negado responde 404 — o mesmo de chave inexistente — para não confirmar a quem tem uma chave alheia que o arquivo existe. O padrão é fechado: sem organização no JWT, prefixo desconhecido ou contexto sem `FileAccessPort` também dão 404.

#### Scenario: Acesso autorizado
- **WHEN** Usuário com acesso ao recurso dono faz `GET /files/{fileKey}`
- **THEN** Resposta 200 com o stream do arquivo, `Content-Type` real gravado no upload e `Content-Disposition: attachment` com o nome original

#### Scenario: Nome original preservado no download
- **WHEN** O arquivo foi enviado como `prova final.pdf`
- **THEN** `Content-Disposition` traz `filename="prova_final.pdf"` (fallback ASCII) e `filename*=UTF-8''prova%20final.pdf` (RFC 5987), com o nome vindo do metadado `original-name` do objeto; objetos anteriores a esse metadado caem no nome embutido na chave

#### Scenario: Arquivo de outra organização
- **WHEN** O JWT traz uma organização diferente da do recurso dono do arquivo
- **THEN** 404 `{"error":"FILE_NOT_FOUND"}`, sem ler o storage

#### Scenario: Usuário da organização sem acesso ao recurso
- **WHEN** Aluno pede material de disciplina fora das suas turmas, anexo de tarefa em rascunho, entrega de outro aluno, ou anexo de aviso de turma da qual não é membro
- **THEN** 404 `{"error":"FILE_NOT_FOUND"}`

#### Scenario: Chave que nenhum recurso referencia
- **WHEN** O objeto existe no bucket, mas nenhum registro ativo da organização aponta para ele (ex.: recurso excluído)
- **THEN** 404 `{"error":"FILE_NOT_FOUND"}`

#### Scenario: Acesso não autenticado
- **WHEN** Request sem JWT válido
- **THEN** 401 Unauthorized

#### Scenario: Arquivo não encontrado
- **WHEN** O acesso foi liberado, mas a chave não existe no storage
- **THEN** `NoSuchKeyException` do SDK vira `FileNotFoundException` do domínio e a resposta é 404 com `{"error":"FILE_NOT_FOUND"}` — antes subia crua como 500

---

### Requirement: Tipos de arquivo por contexto
`StorageContext.LESSON_MATERIAL` aceita: `pdf`, `mp4`, `webm`, `doc`, `docx`. `StorageContext.TASK_ATTACHMENT` aceita: `pdf`, `doc`, `docx`, `zip`, `jpg`, `png` (definido por configuração para facilitar extensão).

#### Scenario: Arquivo com extensão não permitida no contexto
- **WHEN** Upload com MIME type proibido para o contexto
- **THEN** `StoragePort.store()` lança `InvalidFileTypeException`; use case propaga como 422
