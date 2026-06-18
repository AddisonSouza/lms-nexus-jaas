## Context

O módulo `reporting` já existe desde o RF-17 (Admin Dashboard) e RF-18 (Gestor Dashboard): é um módulo somente leitura, sem agregado próprio, que consulta outros módulos via Query Ports implementados com JPQL cross-module por FQN. O RF-19 estende esse módulo para o papel `PROFESSOR`, mas com escopo por **disciplina** (`subjects/{id}`), não por organização ou turma — uma disciplina pode estar vinculada a N turmas (`subject_classrooms`) e um professor pode lecionar N disciplinas (`subject_teachers`).

Diferente de RF-17/RF-18, o controle de acesso aqui não pode ser resolvido só comparando o claim `org` do JWT com um path param: é preciso verificar se o professor autenticado está de fato vinculado à disciplina, via `SubjectRepository.existsSubjectTeacherLink(subjectId, memberId)` (já implementado desde a feature de atribuição de professores). Isso também garante isolamento multi-tenant por construção, pois `AssignTeacherToSubjectService` só permite vincular professor e disciplina da mesma organização.

`Task` já tem `subjectId` direto (não por turma) — as turmas elegíveis de uma tarefa são derivadas via `subject_classrooms`. `TaskSubmission.status` é `SUBMITTED` ou `EVALUATED` (sem status de atraso explícito).

## Goals / Non-Goals

**Goals:**
- `GET /subjects/{id}/dashboard` retorna, para a disciplina informada: contagem de submissões pendentes de avaliação (status `SUBMITTED`) em todas as tarefas da disciplina, distribuição de notas das submissões avaliadas da última tarefa (mais recente por `createdAt`), alunos sem entrega nessa última tarefa, e média de notas por aluno na disciplina.
- Acesso restrito a `PROFESSOR` vinculado à disciplina (`subject_teachers`).
- Indicadores refletem o estado atual de todas as tarefas já criadas na disciplina — sem filtro `from`/`to` (mesmo padrão do RF-18).

**Non-Goals:**
- Dashboard de ALUNO (RF-20).
- Exportação em PDF (não está nos critérios de aceite do RF-19, diferente de RF-17/RF-18; pode ser uma change futura).
- Cache do resultado agregado em Redis.
- Paginação das listas (mesmo padrão do projeto — nenhuma listagem de dashboard pagina ainda).
- Notificação proativa ao professor sobre pendências (apenas exibição no dashboard).

## Decisions

**1. Um único novo Query Port `ProfessorDashboardQueryPort` em `reporting/domain/port/out/`, incluindo o método de verificação de acesso.**
Mantém o padrão já estabelecido (`GestorDashboardQueryPort` no RF-18): um Port por dashboard, métodos cruzando `curriculum`/`assessment` via JPQL direto por FQN. O método de verificação de vínculo professor↔disciplina entra no mesmo Port (`isProfessorAssignedToSubject`) em vez de reabrir uma dependência direta de `reporting` em `SubjectRepository` (do módulo `curriculum`) — mantém `reporting` consultando apenas via seus próprios Ports, como já é o padrão das outras queries cross-module deste módulo.

Métodos do Port:
```java
public interface ProfessorDashboardQueryPort {
    boolean isProfessorAssignedToSubject(String subjectId, String professorId);
    long countPendingEvaluations(String subjectId);
    List<BigDecimal> getLastTaskGradeDistribution(String subjectId);
    List<StudentSummary> getLastTaskStudentsWithoutSubmission(String subjectId);
    List<StudentAverageGrade> getAverageGradePerStudent(String subjectId);
}
```
`StudentSummary` (VO): `studentId`, `studentName`. `StudentAverageGrade` (VO): `studentId`, `studentName`, `averageGrade` (BigDecimal).

**2. `GetProfessorDashboardService` valida o acesso primeiro, lança `UnauthorizedDashboardAccessException` (403) se o professor não estiver vinculado à disciplina.**
Mesma camada de validação usada em `ListTaskSubmissionsService` (assessment) — checagem de autorização na application service, não na camada REST, porque depende de uma consulta (não apenas de um claim do JWT). Alternativa considerada (checagem `org`-claim na REST resource, como em `AdminDashboardResource`/`GestorDashboardResource`) — rejeitada porque aqui o vínculo relevante é professor↔disciplina, não usuário↔organização; comparar apenas `org` do JWT não bastaria (um professor da mesma organização mas que não leciona a disciplina ainda passaria).

**3. Nova exceção `UnauthorizedDashboardAccessException` em `reporting/domain/exception/` (pacote novo neste módulo), implementando `HttpMappable`.**
Primeiro caso de exceção de domínio no módulo `reporting` (RF-17/18 não precisaram, pois usavam checagem de claim na REST resource). Segue o padrão exato de `ContentAccessDeniedException` (curriculum): `httpStatus() = 403`, `errorCode() = "DASHBOARD_ACCESS_DENIED"`.

**4. "Última tarefa" da disciplina = tarefa com maior `createdAt` entre as tarefas com `subjectId` igual à disciplina (sem filtro de status).**
Critério de ordenação simples e alinhado à ordem de publicação do professor (decisão confirmada com o usuário). `ProfessorDashboardQueryPortImpl` resolve o id dessa tarefa internamente (método privado reaproveitado por `getLastTaskGradeDistribution` e `getLastTaskStudentsWithoutSubmission`) — não exposto no Port, pois é um detalhe de implementação que nenhum outro consumidor precisa.

**5. `getLastTaskStudentsWithoutSubmission` calcula o conjunto de alunos elegíveis (matriculados nas turmas vinculadas à disciplina via `subject_classrooms`) menos os alunos com submissão registrada para a última tarefa — mesmo padrão de set-difference do RF-18 (`GestorDashboardQueryPortImpl.listAtRiskStudents`), simplificado: aqui basta "existe submissão ou não", sem comparação de prazo.**

**6. Novo `ProfessorDashboardResource` (`reporting/interfaces/rest/`), em vez de estender `AdminDashboardResource`/`GestorDashboardResource`.**
Path raiz diferente (`/subjects/{id}` vs `/organizations/{id}`) e papel diferente (`PROFESSOR`); mantém o padrão 1 Resource por capability já usado no projeto. Sem checagem de claim na resource — delega inteiramente à exceção lançada pela application service (ponto 2/3).
- `GET /subjects/{id}/dashboard` — `@RolesAllowed("PROFESSOR")`.

**7. Frontend: `SubjectDetailPage` ganha uma seção de dashboard condicional, em vez de uma nova rota.**
`role === 'PROFESSOR'` (mesma condição já usada por `canManage` na página) → renderiza `<ProfessorDashboard subjectId={subjectId} />` acima/abaixo do conteúdo de tópicos existente. Reaproveita o `subjectId` já disponível via `useParams` na página, sem necessidade de rota nova (decisão confirmada com o usuário).

**8. Badge de pendências com polling de 30s no frontend (`useProfessorDashboard`), reaproveitando o intervalo já usado em `useNotifications` (RF-16) — não há infraestrutura de WebSocket/SSE no projeto para push real.**

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/reporting/
  domain/
    model/StudentSummary.java (novo — VO: studentId, studentName)
    model/StudentAverageGrade.java (novo — VO: studentId, studentName, averageGrade)
    exception/UnauthorizedDashboardAccessException.java (novo — HttpMappable, 403)
    port/in/GetProfessorDashboardUseCase.java (novo)
    port/out/ProfessorDashboardQueryPort.java (novo)
  application/
    usecase/GetProfessorDashboardService.java (novo)
    dto/ProfessorDashboardResponse.java, StudentSummaryResponse.java, StudentAverageGradeResponse.java (novos)
  infrastructure/
    persistence/ProfessorDashboardQueryPortImpl.java (novo)
  interfaces/
    rest/ProfessorDashboardResource.java (novo)
```

Nenhuma migration Flyway é necessária (módulo somente leitura, sem entidade própria — mesmo padrão do RF-17/RF-18).

## Endpoints REST

- `GET /subjects/{id}/dashboard` — `@RolesAllowed("PROFESSOR")`. Retorna 403 (via `UnauthorizedDashboardAccessException`) se o professor autenticado não estiver vinculado à disciplina. Retorna 200 com `ProfessorDashboardResponse`.

## Frontend

- **Feature:** `apps/web/src/features/dashboard/` (já existe desde o RF-17)
  - `types.ts`: adiciona `StudentSummary`, `StudentAverageGrade`, `ProfessorDashboardData`
  - `api/professor-dashboard.ts`: `getProfessorDashboard(subjectId)`
  - `api/query-keys.ts`: adiciona `dashboardKeys.professor(subjectId)`
  - `hooks/useProfessorDashboard.ts` (TanStack Query, `refetchInterval: 30_000`)
  - `components/PendingEvaluationsBadge.tsx` (contagem de pendências)
  - `components/LastTaskGradeChart.tsx` (distribuição de notas da última tarefa, reaproveita `recharts` já usado no RF-17)
  - `components/StudentsWithoutSubmissionList.tsx`
  - `components/StudentAverageGradesList.tsx`
  - `components/ProfessorDashboard.tsx` (compõe os componentes acima, recebe `subjectId`)
- **Integração:** `SubjectDetailPage.tsx` (`features/curriculum/components/`) renderiza `<ProfessorDashboard subjectId={subjectId} />` quando `role === 'PROFESSOR'`.
- Sem nova dependência de pacote.

## Risks / Trade-offs

- [Risco] `getLastTaskStudentsWithoutSubmission` e `getLastTaskGradeDistribution` cada uma resolve a "última tarefa" internamente (duas queries para o mesmo id) → [Mitigação] aceitável no volume do MVP; mesmo trade-off já aceito em outras queries do módulo `reporting`.
- [Trade-off] "Última tarefa" considera qualquer status (inclusive `DRAFT`) — se o professor cria uma tarefa rascunho depois de publicar a última, ela passa a ser considerada "última" mesmo sem estar visível aos alunos. Aceito pela simplicidade do critério (decisão confirmada com o usuário); pode ser refinado numa change futura se gerar confusão.
- [Trade-off] Sem filtro de período — indicadores sempre refletem o histórico completo da disciplina, mesmo padrão do RF-18.
- [Trade-off] Sem exportação em PDF — fora do escopo do RF-19 (critérios de aceite não pedem); se necessário no futuro, reaproveitar `DashboardPdfRenderer` como fez o RF-18.
