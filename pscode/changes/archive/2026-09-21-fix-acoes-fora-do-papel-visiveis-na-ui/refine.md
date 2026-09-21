# fix: ações fora do papel visíveis na UI

## Summary

A interface hoje oferece botões e blocos que o papel do usuário logado não pode
usar — clicar só produz erro 403, o que parece defeito do sistema. Esta mudança
faz a UI refletir as permissões que o backend já aplica: cada ação aparece
apenas para quem consegue executá-la.

## Technical detail

Origem: bateria E2E de 15/09/2026 (`docs/qa/E2E_2026-09-15.md`, severidade alta).
As quatro regras já existem no backend; só a UI está desalinhada.

- **Mural** — `ListAnnouncementsService` exige `isMember` na turma, qualquer que
  seja o papel: admin/gestor não são membros e levam 403. Decisão: esconder o
  mural para não-membros. `GET /classrooms/{id}/members` é liberado aos quatro
  papéis e devolve `userId` + `role`, então o próprio frontend decide.
- **"Novo Aviso"** — `PostAnnouncementService` exige membro da turma **com papel
  PROFESSOR**. `AnnouncementFeed.tsx:21` usa `canTeach(role)` (papel da org), que
  é mais largo: gestor/admin veem um botão que sempre falha.
- **"Excluir" disciplina** — `SubjectResource.delete` é `@RolesAllowed(ADMIN_ORG)`,
  mas `SubjectListPage.tsx:29` agrupa a exclusão no mesmo `canManage` de
  `ADMIN_ORG || GESTOR`.
- **"Entrar via código"** — `ClassroomListPage.tsx:38-40` não tem gate nenhum. Não
  há rota dedicada: o formulário é um card inline, então o gate cobre botão e
  formulário. O backend segue aceitando qualquer papel (fora do escopo).
- **Dashboard da Disciplina** — `GetProfessorDashboardService` compara
  `subject_teacher.memberId` com o `userId` do JWT. O `SubjectResponse` expõe só
  `teacherMemberIds`, e listar membros da org é ADMIN_ORG: o frontend não tem como
  decidir. Será adicionado `teacherUserIds` ao DTO (dado novo, mesma autorização).
  Hoje `SubjectDetailPage.tsx:117` renderiza o título e `ProfessorDashboard`
  devolve `null` no 403, deixando um bloco vazio com cabeçalho.
- As checagens ficam centralizadas em `apps/web/src/lib/roles.ts` (hoje só
  `canTeach`/`roleLabel`); as comparações inline espalhadas pelas telas passam a
  consumir os novos helpers.

## Scope

### In

- Esconder na UI: "Novo Aviso", "Excluir" disciplina, "Entrar via código" e o
  bloco "Dashboard da Disciplina".
- Esconder o mural inteiro para quem não é membro da turma.
- Helpers de permissão em `lib/roles.ts`, com testes.
- Adicionar `teacherUserIds` ao `SubjectResponse` (porta, implementação, DTO,
  mapper e teste) — apenas exposição de dado.

### Out

- Mudar regras de autorização do backend: `@RolesAllowed`, `isMember`,
  `isProfessorAssignedToSubject` e `POST /classrooms/join` ficam como estão.
- Liberar o mural para admin/gestor.
- Unificar os rótulos de papel duplicados em quatro arquivos.
- Demais achados da bateria E2E (cards próprios).

## Subtasks

- [x] Centralizar permissões em `lib/roles.ts`: helpers por ação (`canDeleteSubject`,
      `canJoinByCode`, `canManageSubject`) com testes unitários
- [x] Mostrar "Excluir" disciplina só para `ADMIN_ORG` em `SubjectListPage`,
      separando-o do `canManage` de gestor
- [x] Restringir "Entrar via código" a `ALUNO` em `ClassroomListPage`: botão e
      formulário inline
- [x] Esconder o mural para não-membros da turma em `AnnouncementFeed`, via
      `useClassroomMembers`
- [x] Mostrar "Novo Aviso" só para membro da turma com papel `PROFESSOR`,
      substituindo o `canTeach(role)` atual
- [x] Expor `teacherUserIds` no `SubjectResponse` (porta, repositório, DTO, mapper
      e teste de integração)
- [x] Esconder o bloco "Dashboard da Disciplina" para quem não leciona, casando
      `teacherUserIds` com o `userId` do JWT, e eliminar o cabeçalho órfão
