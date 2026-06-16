## Context

RF-12 implementou submissão de tarefas pelo aluno. A tabela `task_submissions` existe (V017) mas não possui campos de avaliação. O módulo `assessment` segue Hexagonal/Clean Arch e o padrão de eventos CDI sem consumer ativo.

Estado atual relevante:
- `TaskSubmission` domain: sem `grade` e `feedback`
- `SubmissionAlreadyEvaluatedException` já existe no domain (placeholder de RF-12)
- Nenhum endpoint de listagem de submissões por tarefa existe
- Frontend: `TaskListPage` (professor) e `StudentTaskListPage` (aluno) existem; nenhum painel de avaliação

## Goals / Non-Goals

**Goals:**
- Adicionar campos `grade`/`feedback` ao modelo e schema
- Endpoint `GET /tasks/{taskId}/submissions` para o professor ver todas as submissões
- Endpoint `PATCH /submissions/{id}/evaluation` para o professor avaliar
- `SubmissionEvaluatedEvent` publicado via CDI
- Frontend: drawer de submissões + formulário de avaliação na `TaskListPage`

**Non-Goals:**
- Consumer de `SubmissionEvaluatedEvent` (RF-16 — Notificações In-App)
- Reavaliação de submissões (status `EVALUATED` é terminal neste RF)
- Registro de ausência/zero para alunos sem submissão
- Download direto de anexos na UI de avaliação (apenas exibição de metadados)

## Decisions

### Migration V019 — ALTER TABLE
Adicionar colunas na tabela existente em vez de nova tabela.
```sql
-- V019__add_evaluation_fields_to_task_submissions.sql
ALTER TABLE task_submissions
  ADD COLUMN grade DECIMAL(5,2) NULL,
  ADD COLUMN feedback LONGTEXT NULL;
```
`grade` nullable para suportar tarefas sem pontuação; `feedback` nullable para permitir avaliação só com nota (embora o spec exija ao menos feedback).

### Endpoint de listagem: GET /tasks/{taskId}/submissions
Reutiliza o path de `/tasks/{taskId}` para manter coerência com a hierarquia REST já estabelecida (submissões são filhas de tasks). Autorização: `PROFESSOR` + verificar `task.createdBy == jwt.sub`.

### Endpoint de avaliação: PATCH /submissions/{id}/evaluation
Path sem prefixo `/tasks/{taskId}` conforme especificado na issue. A verificação de autorização ocorre no use case: carrega a submissão, depois a tarefa associada, verifica `task.createdBy == jwt.sub` e `task.organizationId == jwt.org`.

### Pacotes afetados

**Backend — assessment:**
```
domain/
  model/TaskSubmission.java          ← +grade, +feedback
  event/SubmissionEvaluatedEvent.java ← novo record
  port/in/EvaluateSubmissionUseCase.java ← nova interface
  port/out/SubmissionRepository.java  ← +findByTask(taskId, orgId)
application/
  dto/EvaluateSubmissionCommand.java  ← novo
  dto/SubmissionResponse.java         ← +grade, +feedback
  usecase/EvaluateSubmissionService.java ← novo
infrastructure/
  persistence/TaskSubmissionJpaEntity.java ← +grade, +feedback
  persistence/TaskMapper.java         ← atualizar mapeamento
  persistence/SubmissionRepositoryImpl.java ← +findByTask
interfaces/
  rest/TaskResource.java              ← 2 novos endpoints
```

**Frontend — assessment:**
```
types.ts                            ← +grade, +feedback em TaskSubmission
api/submissions.ts                  ← +listSubmissions(taskId), +evaluateSubmission
api/query-keys.ts                   ← +submissions(taskId)
hooks/useSubmissions.ts             ← novo (lista submissões por tarefa)
hooks/useEvaluateSubmission.ts      ← novo (mutation PATCH)
schemas/evaluation.schema.ts        ← Zod: grade opcional, feedback obrigatório
components/SubmissionListDrawer.tsx ← drawer com lista + botão avaliar
components/EvaluationDialog.tsx     ← dialog com form de avaliação
components/TaskListPage.tsx         ← +botão "Ver Submissões" por tarefa
```

### Endpoint REST summary
| Método | Path | Role | Descrição |
|--------|------|------|-----------|
| GET | `/tasks/{taskId}/submissions` | PROFESSOR | Lista submissões de uma tarefa |
| PATCH | `/submissions/{id}/evaluation` | PROFESSOR | Avalia uma submissão |

## Risks / Trade-offs

- **Autorização no PATCH /submissions/{id}/evaluation** — requer 2 queries (submissão + tarefa). Risco baixo de N+1 dado volume esperado em TCC; aceitável.
- **grade nullable com maxScore null** — tarefa sem pontuação pode ter grade null no response, o que é semanticamente correto mas requer atenção no frontend para não exibir campo vazio.
- **`SubmissionEvaluatedEvent` sem consumer** — o evento é fire-and-forget. Se RF-16 não for implementado, o aluno não recebe notificação. Aceitável para este escopo.
