## ADDED Requirements

### Requirement: Professor cria tópico em disciplina
Um PROFESSOR associado a uma disciplina pode criar tópicos nela para organizar os materiais.

#### Scenario: Criar tópico com sucesso
- **WHEN** PROFESSOR faz `POST /subjects/{subjectId}/topics` com `{ title: "Unidade 1 — Introdução" }`
- **THEN** Tópico criado com `position` auto-incrementado (maior position existente + 1); resposta 201 com id e position

#### Scenario: Criar tópico em disciplina de outra organização
- **WHEN** PROFESSOR faz `POST /subjects/{subjectId}/topics` para disciplina de outra organização
- **THEN** 404 Not Found — a disciplina não é visível fora da organização do JWT

---

### Requirement: Professor reordena tópicos
Professor pode alterar a ordem de exibição dos tópicos via lista de ids.

#### Scenario: Reordenar com sucesso
- **WHEN** PROFESSOR faz `PUT /subjects/{subjectId}/topics/reorder` com `{ topicIds: ["id3","id1","id2"] }`
- **THEN** As posições são atualizadas na ordem enviada; resposta 200 com lista atualizada

#### Scenario: Reordenar com id de outro subject
- **WHEN** Array contém id de tópico de outra disciplina
- **THEN** 400 Bad Request — ids devem pertencer ao subjectId da URL

---

### Requirement: Professor exclui tópico
Exclusão é soft delete. Tópico com conteúdos associados pode ser excluído (conteúdos tornam-se órfãos — soft deleted juntos).

#### Scenario: Excluir tópico sem conteúdo
- **WHEN** PROFESSOR faz `DELETE /subjects/{subjectId}/topics/{id}`
- **THEN** `deleted_at` preenchido no tópico; resposta 204

#### Scenario: Excluir tópico com conteúdos
- **WHEN** Tópico possui conteúdos associados
- **THEN** Tópico e todos os conteúdos do tópico recebem soft delete; resposta 204

---

### Requirement: Aluno visualiza tópicos da disciplina
Aluno só acessa tópicos de disciplinas às quais pertence (via turma).

#### Scenario: Listar tópicos com acesso
- **WHEN** ALUNO faz `GET /subjects/{subjectId}/topics` para disciplina da qual faz parte
- **THEN** Retorna lista de tópicos ativos, ordenados por `position`

#### Scenario: Listar tópicos sem acesso
- **WHEN** ALUNO faz `GET /subjects/{subjectId}/topics` para disciplina que não pertence à sua turma
- **THEN** 403 Forbidden
