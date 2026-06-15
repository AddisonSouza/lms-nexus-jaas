## ADDED Requirements

### Requirement: Professor publica material em tópico
PROFESSOR pode criar conteúdos dos tipos VIDEO, DOCUMENTO, LINK ou ARQUIVO associados a um tópico da disciplina.

#### Scenario: Criar conteúdo do tipo LINK
- **WHEN** PROFESSOR faz `POST /subjects/{id}/contents` com `{ topicId, title, contentType: "LINK", externalUrl: "https://..." }`
- **THEN** Conteúdo criado; resposta 201 com id, type e externalUrl

#### Scenario: Criar conteúdo do tipo VIDEO via URL
- **WHEN** PROFESSOR informa `contentType: "VIDEO"` e `externalUrl: "https://youtube.com/..."`
- **THEN** Conteúdo criado sem upload de arquivo; `fileKey` nulo

#### Scenario: Criar conteúdo do tipo DOCUMENTO via upload
- **WHEN** PROFESSOR faz `POST /subjects/{id}/contents` multipart com arquivo PDF e `contentType: "DOCUMENTO"`
- **THEN** Arquivo armazenado via `StoragePort`; `fileKey` preenchido; resposta 201

#### Scenario: Upload com tipo de arquivo não permitido
- **WHEN** PROFESSOR envia arquivo `.exe` com `contentType: "ARQUIVO"`
- **THEN** 422 Unprocessable Entity — tipo de arquivo rejeitado

#### Scenario: Upload acima de 50MB
- **WHEN** PROFESSOR envia arquivo maior que 50MB
- **THEN** 413 Payload Too Large

---

### Requirement: Materiais listados agrupados por tópico
A listagem de conteúdo de uma disciplina retorna os materiais agrupados por tópico, na ordem definida pelo professor.

#### Scenario: Listar conteúdos como PROFESSOR
- **WHEN** PROFESSOR faz `GET /subjects/{id}/contents`
- **THEN** Resposta com array de tópicos, cada um com lista de conteúdos; ordenados por `position`

#### Scenario: Listar conteúdos como ALUNO com acesso
- **WHEN** ALUNO faz `GET /subjects/{id}/contents` para disciplina da sua turma
- **THEN** Mesma estrutura agrupada retornada

#### Scenario: Listar conteúdos como ALUNO sem acesso
- **WHEN** ALUNO faz `GET /subjects/{id}/contents` para disciplina fora da sua turma
- **THEN** 403 Forbidden

---

### Requirement: Professor exclui material
Exclusão de conteúdo é soft delete. Se o conteúdo possuir arquivo, o arquivo é marcado para deleção no storage mas a operação HTTP retorna 204 independentemente do resultado do storage.

#### Scenario: Excluir conteúdo com arquivo
- **WHEN** PROFESSOR faz `DELETE /subjects/{id}/contents/{contentId}`
- **THEN** `deleted_at` preenchido; arquivo removido do storage via `StoragePort.delete()`; resposta 204
