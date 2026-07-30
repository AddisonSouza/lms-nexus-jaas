# classroom-announcements Specification

## Purpose
Mural de avisos da turma (RF-15): professor publica avisos com texto rico e anexos; alunos visualizam em feed cronológico decrescente e recebem notificação in-app.

## Requirements
### Requirement: Professor publica aviso na turma
O sistema SHALL permitir que um PROFESSOR vinculado à turma publique um aviso com conteúdo em texto rico (obrigatório) e anexos opcionais (arquivo via `StoragePort` e/ou link externo).

#### Scenario: Publicação bem-sucedida apenas com texto
- **WHEN** PROFESSOR vinculado à turma chama `POST /classrooms/{id}/announcements` com `content` preenchido e sem anexos
- **THEN** sistema persiste o aviso, publica `AnnouncementPostedEvent` e retorna 201

#### Scenario: Publicação com anexo de arquivo
- **WHEN** PROFESSOR chama `POST /classrooms/{id}/announcements` com `content` e um arquivo multipart
- **THEN** sistema armazena o arquivo via `StoragePort`, persiste o aviso com `AnnouncementAttachment` correspondente e retorna 201

#### Scenario: Publicação com link externo
- **WHEN** PROFESSOR chama `POST /classrooms/{id}/announcements` com `content` e um link externo (`externalUrl` + `linkTitle`)
- **THEN** sistema persiste o aviso com o anexo de link e retorna 201

#### Scenario: Conteúdo obrigatório ausente
- **WHEN** PROFESSOR chama `POST /classrooms/{id}/announcements` sem `content`
- **THEN** sistema retorna 422

#### Scenario: Professor não vinculado à turma
- **WHEN** PROFESSOR sem vínculo com a turma chama `POST /classrooms/{id}/announcements`
- **THEN** sistema retorna 403

#### Scenario: Aluno tenta publicar aviso
- **WHEN** ALUNO chama `POST /classrooms/{id}/announcements`
- **THEN** sistema retorna 403

### Requirement: Listagem cronológica de avisos
O sistema SHALL retornar os avisos ativos de uma turma ordenados por data de criação decrescente, para qualquer usuário (PROFESSOR ou ALUNO) vinculado à turma.

#### Scenario: Listagem bem-sucedida
- **WHEN** PROFESSOR ou ALUNO vinculado à turma chama `GET /classrooms/{id}/announcements`
- **THEN** sistema retorna 200 com lista de avisos ordenada por `created_at` decrescente

#### Scenario: Turma sem avisos
- **WHEN** usuário vinculado à turma chama `GET /classrooms/{id}/announcements` e não há avisos
- **THEN** sistema retorna 200 com lista vazia

#### Scenario: Usuário não vinculado à turma
- **WHEN** usuário sem vínculo com a turma chama `GET /classrooms/{id}/announcements`
- **THEN** sistema retorna 403

#### Scenario: Avisos excluídos não aparecem na listagem
- **WHEN** um aviso foi excluído (soft delete) e a turma é listada
- **THEN** o aviso excluído não aparece no resultado

### Requirement: Professor edita o próprio aviso
O sistema SHALL permitir que o autor de um aviso edite seu conteúdo e/ou anexos.

#### Scenario: Edição bem-sucedida
- **WHEN** PROFESSOR autor chama `PUT /announcements/{id}` com novo `content`
- **THEN** sistema atualiza o aviso e retorna 200

#### Scenario: Edição por outro professor
- **WHEN** PROFESSOR que não é o autor chama `PUT /announcements/{id}`
- **THEN** sistema retorna 403

#### Scenario: Edição de aviso inexistente
- **WHEN** PROFESSOR chama `PUT /announcements/{id}` para um id inexistente ou já excluído
- **THEN** sistema retorna 404

### Requirement: Professor exclui o próprio aviso
O sistema SHALL permitir que o autor de um aviso o exclua (soft delete).

#### Scenario: Exclusão bem-sucedida
- **WHEN** PROFESSOR autor chama `DELETE /announcements/{id}`
- **THEN** sistema marca `deleted_at` e retorna 204

#### Scenario: Exclusão por outro professor
- **WHEN** PROFESSOR que não é o autor chama `DELETE /announcements/{id}`
- **THEN** sistema retorna 403

#### Scenario: Aluno tenta excluir aviso
- **WHEN** ALUNO chama `DELETE /announcements/{id}`
- **THEN** sistema retorna 403
