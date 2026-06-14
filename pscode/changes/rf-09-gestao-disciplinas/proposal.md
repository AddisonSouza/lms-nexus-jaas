## Why

O módulo `curriculum` ainda não existe. Sem disciplinas, não é possível organizar conteúdo, tarefas ou avaliações por área de conhecimento — bloqueando RF-10 a RF-14 que dependem de `subject_id`. Implementar RF-09 desbloqueia o restante do currículo.

## What Changes

- **Novo módulo `curriculum`** (BE): domínio, ports, use cases, persistência e REST para `Subject`.
- **CRUD completo de disciplinas**: `POST`, `GET` (list + detail), `PUT` e `DELETE` (soft) em `/subjects`.
- **Vínculo disciplina ↔ turma**: `POST /subjects/{id}/classrooms` registra em `subject_classrooms`.
- **Vínculo disciplina ↔ professor**: `POST /subjects/{id}/teachers` registra em `subject_teachers` (nível da disciplina na organização, compatível com RN-06).
- **Nova feature FE `features/curriculum`**: lista, formulário de criação/edição, dialogs de vínculo (turma e professor).
- **Migrations Flyway V010–V012**: tabelas `subjects`, `subject_classrooms`, `subject_teachers`.

## Capabilities

### New Capabilities

- `subject-management`: CRUD de disciplinas dentro de uma organização (ADMIN_ORG/GESTOR). Campos: nome, código/sigla, descrição, carga horária. Soft delete. Escopo: `organization_id` do JWT.
- `subject-classroom-link`: Vínculo N:M entre disciplina e turma via `subject_classrooms`. Somente turmas da mesma organização.
- `subject-teacher-assignment`: Vínculo N:M entre disciplina e professor via `subject_teachers`. Professor deve ser membro da organização com papel PROFESSOR.

### Modified Capabilities

_(nenhuma)_

## Impact

- **Backend**: novo módulo `curriculum` em `apps/api/src/main/java/br/edu/lms/module/curriculum/`.
- **Frontend**: nova feature `apps/web/src/features/curriculum/`.
- **Banco**: 3 novas tabelas (`subjects`, `subject_classrooms`, `subject_teachers`), próximas migrations V010–V012.
- **Dependências**: `classroom` (leitura de turmas para vínculo), `organization` (verificação de membros PROFESSOR).
- **Non-goals**: conteúdo complementar (RF-10), tarefas (RF-11), dashboards, notificações.
