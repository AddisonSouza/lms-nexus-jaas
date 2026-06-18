## 1. Backend — Domínio e Ports

- [x] 1.1 [BE] Criar `ClassroomHealth` (record: `classroomId`, `classroomName`, `status`, `deliveryRate`, `averageGrade` nullable) em `reporting/domain/model/`
- [x] 1.2 [BE] Criar `AtRiskStudent` (record: `studentId`, `studentName`, `pendingCount`) em `reporting/domain/model/`
- [x] 1.3 [BE] Criar porta de entrada `GetGestorDashboardUseCase` em `reporting/domain/port/in/`
- [x] 1.4 [BE] Criar porta de entrada `ExportGestorDashboardPdfUseCase` em `reporting/domain/port/in/`
- [x] 1.5 [BE] Criar porta de saída `GestorDashboardQueryPort` (`getClassroomsHealth`, `listAtRiskStudents`) em `reporting/domain/port/out/`

## 2. Backend — Infraestrutura (Query Port)

- [x] 2.1 [BE] Implementar `GestorDashboardQueryPortImpl.getClassroomsHealth` (JPQL FQN contra `ClassroomJpaEntity` filtrado por `organizationId`; `deliveryRate` por turma reaproveitando a lógica de `TaskMetricsQueryPortImpl.averageDeliveryRate` do RF-17, escopada por `classroomId` via `SubjectClassroomJpaEntity`; `averageGrade` = média de `grade` das submissões `EVALUATED` das tarefas da turma, `null` se não houver nenhuma)
- [x] 2.2 [BE] Implementar `GestorDashboardQueryPortImpl.listAtRiskStudents` (para a turma: alunos via `ClassroomMemberJpaEntity` role `ALUNO`; conta tarefas vinculadas com `deadline < NOW()` sem submissão do aluno ou com submissão criada após o `deadline`; ordena decrescente, aplica `limit`)

## 3. Backend — Use Cases e DTOs

- [x] 3.1 [BE] Criar `GestorDashboardResponse`, `ClassroomHealthResponse`, `AtRiskStudentResponse` em `reporting/application/dto/`
- [x] 3.2 [BE] Implementar `GetGestorDashboardService`: chama `getClassroomsHealth`, depois `listAtRiskStudents(classroomId, 5)` por turma, monta `GestorDashboardResponse`
- [x] 3.3 [BE] Adicionar `renderGestorDashboard(GestorDashboardResponse)` a `DashboardPdfRenderer` com novo `@Location("reporting/gestor-dashboard.html") Template`
- [x] 3.4 [BE] Implementar `ExportGestorDashboardPdfService`: reutiliza `GetGestorDashboardService` e delega a `DashboardPdfRenderer.renderGestorDashboard`
- [x] 3.5 [BE] Criar template Qute `templates/reporting/gestor-dashboard.html` (tabela de turmas: status, % entrega, média de notas, lista de alunos em risco por turma)

## 4. Backend — Endpoint REST

- [x] 4.1 [BE] Criar `GestorDashboardResource` com `GET /organizations/{id}/gestor-dashboard` (`@RolesAllowed("GESTOR")`, valida `id == jwt.org`, 403 caso contrário)
- [x] 4.2 [BE] Adicionar `GET /organizations/{id}/gestor-dashboard/pdf` (mesmas regras de acesso, `Content-Type: application/pdf`)

## 5. Backend — Testes

- [x] 5.1 [BE] Testes unitários de `GetGestorDashboardService` (agregação correta por turma, turma sem submissões avaliadas retorna `averageGrade = null`, organização sem turmas retorna lista vazia)
- [x] 5.2 [BE] Testes de integração `@QuarkusTest` de `GestorDashboardQueryPortImpl` (`getClassroomsHealth` e `listAtRiskStudents` cobrindo: turma com entregas/notas, turma sem submissões avaliadas, aluno com tarefas pendentes/atrasadas, aluno sem pendências, limit do top N)
- [x] 5.3 [BE] Testes de integração `@QuarkusTest` para `GestorDashboardResource` cobrindo os cenários da spec (sucesso, isolamento por organização, papel não autorizado)
- [x] 5.4 [BE] Teste de integração do endpoint de PDF (`Content-Type` correto, 403 para papel/organização incorretos)

## 6. Frontend — Tipos e API

- [x] 6.1 [FE] Adicionar `ClassroomHealth`, `AtRiskStudent`, `GestorDashboardData` a `features/dashboard/types.ts`
- [x] 6.2 [FE] Criar `features/dashboard/api/gestor-dashboard.ts` com `getGestorDashboard(organizationId)` e `exportGestorDashboardPdf(organizationId)`
- [x] 6.3 [FE] Adicionar `dashboardKeys.gestor(organizationId)` a `features/dashboard/api/query-keys.ts`

## 7. Frontend — Hooks e componentes

- [x] 7.1 [FE] Criar hook `useGestorDashboard(organizationId)` (TanStack Query)
- [x] 7.2 [FE] Criar `ClassroomHealthCards.tsx` (cards comparativos por turma: status, % entrega, média de notas — trata `averageGrade = null` como "Sem notas ainda")
- [x] 7.3 [FE] Criar `AtRiskStudentsList.tsx` (lista dos alunos em risco por turma)
- [x] 7.4 [FE] Criar `GestorDashboard.tsx` compondo os componentes acima + botão "Exportar PDF" (mesmo padrão do `AdminDashboard` do RF-17)
- [x] 7.5 [FE] Atualizar `OrganizationDashboardPage.tsx`: renderizar `<GestorDashboard organizationId={id} />` quando `useAuthStore((s) => s.role) === 'GESTOR'`, mantendo o comportamento atual para os demais papéis

## 8. Frontend — Testes

- [x] 8.1 [FE] Testes Vitest + Testing Library para `ClassroomHealthCards` (renderização de dados, `averageGrade` nulo)
- [x] 8.2 [FE] Testes para `AtRiskStudentsList` (lista com alunos, lista vazia)
- [x] 8.3 [FE] Atualizar teste de `OrganizationDashboardPage` cobrindo a renderização condicional para `GESTOR`
