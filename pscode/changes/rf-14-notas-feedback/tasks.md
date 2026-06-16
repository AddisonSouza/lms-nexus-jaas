## 1. Backend — Port e DTO

- [ ] 1.1 Adicionar `findByStudentAndOrganization(String studentId, String organizationId): List<TaskSubmission>` à interface `SubmissionRepository`
- [ ] 1.2 Criar `TaskWithGradeResponse` DTO em `assessment/application/dto/` com campos: task info (id, title, description, deadline, maxScore, status) + submission aninhado (id, status, grade, feedback, submittedAt, lateSubmission)
- [ ] 1.3 Criar porta de entrada `ListStudentGradesUseCase` em `assessment/domain/port/in/`

## 2. Backend — Use Case

- [ ] 2.1 Implementar `ListStudentGradesService`: busca tarefas publicadas da org (`taskRepository.findPublishedByOrganization`), busca submissões do aluno via `submissionRepository.findByStudentAndOrganization`, monta mapa taskId→submission, combina em `List<TaskWithGradeResponse>` com `lateSubmission` calculado
- [ ] 2.2 Implementar `findByStudentAndOrganization` em `SubmissionRepositoryImpl` com JPQL: `SELECT s FROM TaskSubmissionJpaEntity s WHERE s.studentId = :studentId AND s.organizationId = :orgId AND s.deletedAt IS NULL`

## 3. Backend — Endpoints REST

- [ ] 3.1 Adicionar `GET /tasks/my-grades` em `TaskResource`: `@RolesAllowed("ALUNO")`, extrai `studentId` e `orgId` do JWT, delega ao `ListStudentGradesUseCase`, retorna `List<TaskWithGradeResponse>`
- [ ] 3.2 Adicionar `GET /submissions/{id}/feedback` em `SubmissionResource`: `@RolesAllowed("ALUNO")`, verifica ownership (403 se não é do aluno), verifica status EVALUATED (409 se SUBMITTED), retorna `SubmissionResponse`

## 4. Frontend — Tipos e API

- [ ] 4.1 Adicionar tipo `TaskWithGrade` em `features/assessment/types.ts` espelhando `TaskWithGradeResponse` do backend (campos task + `submission: TaskSubmission | null`)
- [ ] 4.2 Adicionar função `listStudentGrades(): Promise<TaskWithGrade[]>` em `features/assessment/api/submissions.ts` chamando `GET /tasks/my-grades`
- [ ] 4.3 Criar hook `useStudentGrades` em `features/assessment/hooks/useStudentGrades.ts` com TanStack Query

## 5. Frontend — Componentes

- [ ] 5.1 Criar `GradeFeedbackDrawer` em `features/assessment/components/GradeFeedbackDrawer.tsx`: drawer lateral com nota (badge colorido por aprovação), feedback textual, data de envio e badge "Atrasado" quando `lateSubmission: true`
- [ ] 5.2 Atualizar `StudentTaskListPage`: trocar `useStudentTasks` por `useStudentGrades`; exibir badge de status por tarefa (Não enviado / Enviado / Avaliado); mostrar nota em linha quando avaliado; botão "Ver Nota" abre `GradeFeedbackDrawer`; manter botão "Enviar Resposta" / "Editar Resposta" quando aplicável
