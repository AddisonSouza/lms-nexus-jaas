## Why

RF-15 (módulo `communication`) exige um mural de avisos por turma: professores publicam comunicados que alunos visualizam em ordem cronológica decrescente, como já ocorre em ferramentas equivalentes de LMS. Hoje não existe nenhum canal de comunicação assíncrona entre professor e turma — apenas conteúdo didático (RF-10) e tarefas (RF-11/12/13/14). Sem o mural, professores não têm como avisar a turma sobre eventos, mudanças de prazo ou recados gerais.

## What Changes

- Cria o módulo `communication` (novo Bounded Context) com o agregado `Announcement`.
- PROFESSOR pode publicar, editar e excluir (soft delete) avisos em turmas onde leciona.
- ALUNO visualiza avisos das turmas às quais pertence, em ordem cronológica decrescente (mais recente primeiro).
- Aviso suporta conteúdo em texto rico (obrigatório) e anexos opcionais (arquivos via `StoragePort` existente, ou links externos).
- Publica `AnnouncementPostedEvent` ao criar um aviso (sem consumidor ainda — RF-16/notificações é capability futura; segue o mesmo padrão já usado por `TaskSubmittedEvent`/`SubmissionEvaluatedEvent`, publicados via CDI `Event<T>` antes de existir um consumidor).
- Frontend: nova feature `features/communication` com feed de avisos na página da turma, formulário de criação/edição (PROFESSOR) e visualização somente leitura (ALUNO).
- Lista sem paginação, seguindo o padrão atual do projeto (nenhum outro endpoint de listagem usa paginação ainda).

## Capabilities

### New Capabilities
- `classroom-announcements`: publicação, edição, exclusão (soft delete) e listagem cronológica de avisos por turma, com anexos opcionais e controle de acesso por papel (PROFESSOR autor / ALUNO leitor, ambos restritos a turmas das quais são membros).

### Modified Capabilities
(nenhuma — funcionalidade nova, não altera contratos de specs existentes)

## Impact

- **Backend**: novo módulo `communication` completo (domain/application/infrastructure/interfaces), nova tabela `announcements` (+ `announcement_attachments`) via migration Flyway, novo `ClassroomQueryPort` em `communication/domain/port/out/` para validar vínculo do usuário com a turma (mesmo padrão de `ClassroomQueryPort` já usado no `curriculum`), reuso do `StoragePort` existente para upload de anexos.
- **Frontend**: nova feature `features/communication` (componentes, hooks TanStack Query, schema Zod), nova rota/seção na página da turma.
- **Sem impacto** em módulos `assessment`, `curriculum`, `classroom`, `organization`, `identity` além da leitura via Port.
