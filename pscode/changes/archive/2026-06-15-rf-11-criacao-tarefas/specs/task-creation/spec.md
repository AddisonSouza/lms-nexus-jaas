## ADDED Requirements

### Requirement: Professor cria tarefa em rascunho
O sistema SHALL permitir que um usuário com papel `PROFESSOR` crie uma tarefa (`DRAFT`) vinculada a um Subject existente, fornecendo título (obrigatório), enunciado em Markdown (obrigatório), prazo (obrigatório, data futura), pontuação máxima (opcional) e zero ou mais anexos do tipo `task_attachment` (pdf, doc, docx, zip, jpg, png).

#### Scenario: Criação bem-sucedida sem anexos
- **WHEN** professor envia `POST /tasks` com título, enunciado, prazo futuro e subjectId válido
- **THEN** sistema cria a tarefa com status `DRAFT`, retorna `201 Created` com o recurso criado

#### Scenario: Criação com anexos
- **WHEN** professor envia `POST /tasks` com campos válidos e até N arquivos de tipos permitidos
- **THEN** sistema persiste os anexos via `StoragePort` com contexto `task_attachment` e associa à tarefa

#### Scenario: Prazo no passado
- **WHEN** professor envia prazo com data igual ou anterior à data atual
- **THEN** sistema rejeita com `400 Bad Request` e mensagem indicando que o prazo deve ser futuro

#### Scenario: Tipo de arquivo inválido no anexo
- **WHEN** professor inclui arquivo com extensão não permitida (ex: `.exe`, `.sh`)
- **THEN** sistema rejeita com `422 Unprocessable Entity` indicando o tipo inválido

#### Scenario: Professor não vinculado ao Subject
- **WHEN** professor tenta criar tarefa para um Subject ao qual não está atribuído
- **THEN** sistema rejeita com `403 Forbidden`

#### Scenario: Aluno tenta criar tarefa
- **WHEN** usuário com papel `ALUNO` envia `POST /tasks`
- **THEN** sistema rejeita com `403 Forbidden`

### Requirement: Tarefa DRAFT invisível para alunos
O sistema SHALL garantir que tarefas com status `DRAFT` sejam invisíveis para usuários com papel `ALUNO`.

#### Scenario: Aluno tenta acessar tarefa em rascunho
- **WHEN** aluno realiza qualquer operação de leitura em uma tarefa com status `DRAFT`
- **THEN** sistema retorna `404 Not Found` (sem revelar a existência)
