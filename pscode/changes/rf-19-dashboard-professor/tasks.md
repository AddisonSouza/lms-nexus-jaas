## 1. Backend — Domínio e Ports

- [ ] 1.1 [BE] Criar `StudentSummary` (record: `studentId`, `studentName`) em `reporting/domain/model/`
- [ ] 1.2 [BE] Criar `StudentAverageGrade` (record: `studentId`, `studentName`, `averageGrade`) em `reporting/domain/model/`
- [ ] 1.3 [BE] Criar `UnauthorizedDashboardAccessException` (HttpMappable, `httpStatus() = 403`, `errorCode() = "DASHBOARD_ACCESS_DENIED"`) em `reporting/domain/exception/`
- [ ] 1.4 [BE] Criar porta de entrada `GetProfessorDashboardUseCase` em `reporting/domain/port/in/`
- [ ] 1.5 [BE] Criar porta de saída `ProfessorDashboardQueryPort` (`isProfessorAssignedToSubject`, `countPendingEvaluations`, `getLastTaskGradeDistribution`, `getLastTaskStudentsWithoutSubmission`, `getAverageGradePerStudent`) em `reporting/domain/port/out/`

## 2. Backend — Infraestrutura (Query Port)

- [ ] 2.1 [BE] Implementar `ProfessorDashboardQueryPortImpl.isProfessorAssignedToSubject` (JPQL FQN contra `SubjectTeacherJpaEntity`, mesma checagem de `SubjectRepositoryImpl.existsSubjectTeacherLink`)
- [ ] 2.2 [BE] Implementar método privado de resolução da "última tarefa" da disciplina (maior `createdAt` entre `TaskJpaEntity` com `subjectId` igual à disciplina), reaproveitado pelos métodos 2.3 e 2.4
- [ ] 2.3 [BE] Implementar `ProfessorDashboardQueryPortImpl.countPendingEvaluations` (`COUNT` de `TaskSubmissionJpaEntity` com `status = 'SUBMITTED'`, `deletedAt IS NULL`, para todas as tarefas da disciplina)
- [ ] 2.4 [BE] Implementar `ProfessorDashboardQueryPortImpl.getLastTaskGradeDistribution` (lista de `grade` das submissões `EVALUATED` da última tarefa da disciplina)
- [ ] 2.5 [BE] Implementar `ProfessorDashboardQueryPortImpl.getLastTaskStudentsWithoutSubmission` (alunos elegíveis via `ClassroomMemberJpaEntity` role `ALUNO` nas turmas vinculadas à disciplina via `SubjectClassroomJpaEntity`, menos os alunos com submissão registrada na última tarefa)
- [ ] 2.6 [BE] Implementar `ProfessorDashboardQueryPortImpl.getAverageGradePerStudent` (média de `grade` por `studentId`, considerando todas as submissões `EVALUATED` de tarefas da disciplina)

## 3. Backend — Use Case e DTOs

- [ ] 3.1 [BE] Criar `ProfessorDashboardResponse`, `StudentSummaryResponse`, `StudentAverageGradeResponse` em `reporting/application/dto/`
- [ ] 3.2 [BE] Implementar `GetProfessorDashboardService`: valida `isProfessorAssignedToSubject` (lança `UnauthorizedDashboardAccessException` caso negativo), depois chama os demais métodos do Port e monta `ProfessorDashboardResponse`

## 4. Backend — Endpoint REST

- [ ] 4.1 [BE] Criar `ProfessorDashboardResource` com `GET /subjects/{id}/dashboard` (`@RolesAllowed("PROFESSOR")`, sem checagem de claim na resource — delega à exceção da application service)

## 5. Backend — Testes

- [ ] 5.1 [BE] Testes unitários de `GetProfessorDashboardService` (professor vinculado retorna dashboard completo; professor não vinculado lança `UnauthorizedDashboardAccessException`; disciplina sem tarefas retorna indicadores vazios/zero)
- [ ] 5.2 [BE] Testes de integração `@QuarkusTest` de `ProfessorDashboardQueryPortImpl` cobrindo: pendências de avaliação, distribuição de notas da última tarefa (com e sem submissões avaliadas), alunos sem entrega na última tarefa (com e sem alunos faltantes), média de notas por aluno
- [ ] 5.3 [BE] Testes de integração `@QuarkusTest` para `ProfessorDashboardResource` cobrindo os cenários da spec (sucesso, professor não vinculado → 403, papel não autorizado → 403)

## 6. Frontend — Tipos e API

- [ ] 6.1 [FE] Adicionar `StudentSummary`, `StudentAverageGrade`, `ProfessorDashboardData` a `features/dashboard/types.ts`
- [ ] 6.2 [FE] Criar `features/dashboard/api/professor-dashboard.ts` com `getProfessorDashboard(subjectId)`
- [ ] 6.3 [FE] Adicionar `dashboardKeys.professor(subjectId)` a `features/dashboard/api/query-keys.ts`

## 7. Frontend — Hooks e componentes

- [ ] 7.1 [FE] Criar hook `useProfessorDashboard(subjectId)` (TanStack Query, `refetchInterval: 30_000`, mesmo padrão de `useNotifications`)
- [ ] 7.2 [FE] Criar `PendingEvaluationsBadge.tsx` (contagem de submissões pendentes de avaliação)
- [ ] 7.3 [FE] Criar `LastTaskGradeChart.tsx` (distribuição de notas da última tarefa, reaproveita `recharts`; trata lista vazia como "Sem notas ainda")
- [ ] 7.4 [FE] Criar `StudentsWithoutSubmissionList.tsx` (lista de alunos sem entrega na última tarefa; trata lista vazia)
- [ ] 7.5 [FE] Criar `StudentAverageGradesList.tsx` (média de notas por aluno na disciplina)
- [ ] 7.6 [FE] Criar `ProfessorDashboard.tsx` compondo os componentes acima, recebendo `subjectId`
- [ ] 7.7 [FE] Atualizar `SubjectDetailPage.tsx`: renderizar `<ProfessorDashboard subjectId={subjectId} />` quando `useAuthStore((s) => s.role) === 'PROFESSOR'`, mantendo o comportamento atual para os demais papéis

## 8. Frontend — Testes

- [ ] 8.1 [FE] Testes Vitest + Testing Library para `LastTaskGradeChart` (renderização de dados, lista vazia)
- [ ] 8.2 [FE] Testes para `StudentsWithoutSubmissionList` (lista com alunos, lista vazia)
- [ ] 8.3 [FE] Testes para `PendingEvaluationsBadge` e `StudentAverageGradesList`
- [ ] 8.4 [FE] Atualizar teste de `SubjectDetailPage` cobrindo a renderização condicional para `PROFESSOR`
