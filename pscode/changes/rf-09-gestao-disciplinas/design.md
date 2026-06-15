## Context

O módulo `curriculum` é o quarto bounded context do LMS e o primeiro não implementado. Depende de `classroom` (leitura de turmas) e `organization` (verificação de membros) via interfaces Java (Ports), nunca HTTP interno. O módulo `assessment` (RF-11) depende de `curriculum` — esta change desbloqueia o restante da pilha de conteúdo.

Último migration existente: `V009__add_unique_index_classrooms_invite_code.sql`. Novos migrations partem de V010.

## Goals / Non-Goals

**Goals:**
- Criar o módulo `curriculum` completo: domain, application, infrastructure e REST.
- CRUD de `Subject` (soft delete, scoped por `organization_id` do JWT).
- Vínculos N:M: `subject_classrooms` e `subject_teachers`.
- Feature FE `features/curriculum` com lista, formulário, dialogs de vínculo.

**Non-Goals:**
- Conteúdo complementar (RF-10) — `SubjectTopic` e uploads ficam em change separada.
- Dashboards (RF-17 a RF-20) — queries de reporting fora de escopo.
- Notificações — nenhum `DomainEvent` publicado neste RF.

## Decisions

### D1 — Estrutura de pacotes idêntica ao módulo `classroom`

Seguir exatamente o mesmo layout de `classroom/` garante consistência no monolito. Cada camada tem fronteira explícita; regras `RD-01 a RD-08` são contratos.

```
module/curriculum/
  domain/model/           Subject, SubjectId, SubjectCode
  domain/exception/       SubjectNotFoundException, SubjectAlreadyLinkedException
  domain/port/in/         CreateSubjectUseCase, UpdateSubjectUseCase, ...
  domain/port/out/        SubjectRepository
  application/dto/        CreateSubjectCommand, SubjectResponse, ...
  application/usecase/    CreateSubjectService, LinkSubjectToClassroomService, ...
  infrastructure/persistence/  SubjectJpaEntity, SubjectClassroomJpaEntity, SubjectTeacherJpaEntity, SubjectRepositoryImpl
  interfaces/rest/        SubjectResource
  interfaces/rest/dto/    CreateSubjectRequest, UpdateSubjectRequest, LinkClassroomRequest, AssignTeacherRequest
```

### D2 — Professor vinculado no nível da disciplina (subject-level)

RN-06: "Um professor pode estar vinculado a múltiplas disciplinas na mesma organização." A granularidade é disciplina × organização, não disciplina × turma. Tabela `subject_teachers (subject_id, member_id)` sem classroom_id. Se no futuro precisar de granularidade por turma, adicionamos coluna sem quebrar a API atual.

Alternativa descartada: `subject_classroom_teachers (subject_id, classroom_id, member_id)` — mais granular mas contraria RN-06 e complica o endpoint `POST /subjects/{id}/teachers`.

### D3 — Acesso cross-module via Port (ClassroomQueryPort)

`LinkSubjectToClassroomService` precisa verificar se a turma existe e pertence à organização. Solução: definir `ClassroomQueryPort` em `curriculum/domain/port/out/` com método `existsByIdAndOrganizationId(classroomId, orgId): boolean`. `classroom` module provê a implementação. Sem chamadas HTTP internas (MOD-01).

### D4 — Migrations Flyway

| Arquivo | Conteúdo |
|---|---|
| `V010__create_subjects_table.sql` | `subjects (id, name, code, description, workload_hours, organization_id, deleted_at, ...)` |
| `V011__create_subject_classrooms_table.sql` | `subject_classrooms (subject_id, classroom_id, created_at)` — PK composta |
| `V012__create_subject_teachers_table.sql` | `subject_teachers (subject_id, member_id, created_at)` — PK composta |

### D5 — Endpoints REST

| Método | Path | Roles | Descrição |
|---|---|---|---|
| POST | `/subjects` | ADMIN_ORG, GESTOR | Criar disciplina |
| GET | `/subjects` | ADMIN_ORG, GESTOR, PROFESSOR | Listar disciplinas da org |
| GET | `/subjects/{id}` | ADMIN_ORG, GESTOR, PROFESSOR | Detalhar disciplina |
| PUT | `/subjects/{id}` | ADMIN_ORG, GESTOR | Atualizar disciplina |
| DELETE | `/subjects/{id}` | ADMIN_ORG | Soft delete |
| POST | `/subjects/{id}/classrooms` | ADMIN_ORG, GESTOR | Vincular turma |
| DELETE | `/subjects/{id}/classrooms/{classroomId}` | ADMIN_ORG, GESTOR | Desvincular turma |
| POST | `/subjects/{id}/teachers` | ADMIN_ORG, GESTOR | Atribuir professor |
| DELETE | `/subjects/{id}/teachers/{memberId}` | ADMIN_ORG, GESTOR | Remover professor |

### D6 — FE: features/curriculum

Seguir o padrão de `features/classroom`:
- `api/subject-api.ts` + `api/query-keys.ts`
- `hooks/useSubjects`, `useSubject`, `useCreateSubject`, `useUpdateSubject`, `useDeleteSubject`, `useLinkClassroom`, `useAssignTeacher`
- `components/SubjectListPage`, `SubjectFormDialog`, `LinkClassroomDialog`, `AssignTeacherDialog`
- `schemas/subjectSchema.ts` (Zod)
- `types.ts`

## Risks / Trade-offs

- **[Risco] ClassroomQueryPort cria dependência unidirecional `curriculum → classroom`** → Mitigação: interface definida em `curriculum/domain/port/out/`, implementada em `classroom/infrastructure/`. Dependência segue a direção correta (MOD-04).
- **[Trade-off] Sem paginação nos GETs** → Aceitável para MVP (orgs têm poucas disciplinas). Paginação adicionada em change futura sem quebrar contratos.
- **[Risco] Soft delete em `subjects` não cascateia para vínculos** → Ao soft-deletar uma disciplina, os vínculos em `subject_classrooms` e `subject_teachers` permanecem mas ficam inacessíveis (filtro `deleted_at IS NULL` no repository). RF-09 não requer limpeza de vínculos.

## Migration Plan

1. Deploy com migrations V010–V012 (additive — sem ALTER de colunas existentes).
2. Sem rollback automático: migrations são cumulativas. Rollback = novos migrations V013+ que dropam as tabelas novas.
3. Sem dado de produção a migrar — módulo novo.
