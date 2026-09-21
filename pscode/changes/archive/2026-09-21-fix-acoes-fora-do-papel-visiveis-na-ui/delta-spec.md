# fix: ações fora do papel visíveis na UI — Delta

## Changed

Cada ação passa a aparecer só para quem o backend deixa executá-la. Nenhuma regra
de autorização mudou — `@RolesAllowed`, `isMember` e
`isProfessorAssignedToSubject` seguem como estavam; o que mudou foi a UI parar de
oferecer o que ia virar 403.

- **Excluir disciplina** só para `ADMIN_ORG` (`SubjectResource.delete` é
  `@RolesAllowed(ADMIN_ORG)`). O gestor mantém criar e editar: o `canManage` que
  agrupava as três ações virou `canManageSubject` + `canDeleteSubject`.
- **"Entrar via código"** só para `ALUNO`, botão **e** o formulário inline que ele
  abre — não há rota dedicada, então o gate cobre os dois.
- **Mural de avisos** some inteiro para quem não é membro da turma.
  `ListAnnouncementsService` exige associação seja qual for o papel na
  organização, então admin e gestor levavam 403. A busca também deixa de disparar
  (`useAnnouncements(classroomId, isMember)`), e o cabeçalho "Mural de Avisos" sai
  junto com o bloco.
- **"Novo Aviso"** exige ser membro da turma **com papel PROFESSOR nela**
  (`PostAnnouncementService`), no lugar do `canTeach(role)` do papel da
  organização, largo demais.
- **Bloco "Dashboard da Disciplina"** só para quem leciona aquela disciplina,
  casando `teacherUserIds` com o `userId` do JWT. Antes saía pelo papel da
  organização e, no 403, `ProfessorDashboard` devolvia `null` — sobrava o
  cabeçalho sobre um bloco vazio.

## Added

- **`teacherUserIds` no `SubjectResponse`** — apenas exposição de dado, mesma
  autorização. O vínculo guarda `memberId` e o JWT carrega `userId`; listar
  membros da organização é privilégio de `ADMIN_ORG`, então o front não tinha como
  traduzir. `OrganizationMemberQueryPort.findUserIdsByMemberIds` faz a ponte, e
  `GET /subjects`, `GET /subjects/{id}`, `POST` e `PUT` passam a devolvê-lo (lista
  vazia quando não há professor, nunca nula).
- **Gates por ação em `lib/roles.ts`** (`canDeleteSubject`, `canManageSubject`,
  `canJoinByCode`), com testes. As comparações inline espalhadas pelas telas
  divergiam da regra do servidor.
- Testes: `roles` (8), o par admin/gestor do "Excluir", aluno/admin do "Entrar via
  código", não-membro no `AnnouncementFeed`, o novo `ClassroomDetailPage.test.tsx`
  do cabeçalho órfão, e `SubjectTeacherUserIdsIT` (3) na API.

## Removed

- Nada.

## Decisões que desviaram do refine

- **A associação chega ao `AnnouncementFeed` por prop, não por
  `useClassroomMembers`.** O hook vive em `features/classroom` e o mural em
  `features/communication`: consumi-lo direto seria import cruzado entre features,
  proibido pelo `DECISIONS.md`. `ClassroomDetailRoute`, na camada `app`, resolve a
  associação e entrega `isMember`/`canPost` prontos.
- **Testes que fixavam o comportamento antigo foram reescritos**, não removidos:
  `SubjectDetailRoute.test.tsx` afirmava que gestor e admin viam o painel da
  disciplina, e `AnnouncementFeed.test.tsx` que gestor via "Novo Aviso".

## Conhecido, fora deste card

- **`POST /classrooms/join` continua aceitando qualquer papel.** O gate de
  "Entrar via código" é só de interface; mudar a regra estava fora do escopo.
- O mural segue fechado para admin e gestor — liberar para eles foi decisão
  explícita do refine, não omissão.
- Rótulos de papel duplicados em quatro arquivos continuam por unificar.
