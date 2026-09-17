# fix: aluno sem caminho até os materiais

## Summary

Hoje o aluno não tem como chegar aos materiais das suas disciplinas: a sidebar
não mostra "Disciplinas" para o papel ALUNO e `/curriculum` responde 403, que a
tela exibe como falha de conexão/sessão expirada. A mudança abre a listagem de
disciplinas para o aluno — restrita às disciplinas das turmas de que ele é
membro — e corrige a mensagem de erro de permissão.

## Technical detail

- `SubjectResource.list()` e `getById()` hoje são `@RolesAllowed({"ADMIN_ORG",
  "GESTOR","PROFESSOR"})`. `ContentResource.list()` e `TopicResource.list()` já
  incluem `ALUNO` — a lacuna está só em `/subjects`.
- O padrão de autorização já existe: `ListSubjectContentsService` chama
  `classroomQueryPort.isMemberOfAnyClassroom(userId, classroomIds, orgId)` e
  lança `ContentAccessDeniedException` (403, `CONTENT_ACCESS_DENIED`). As novas
  checagens reusam esse mecanismo.
- Para a listagem, filtrar por assinatura evita N+1: nova query
  `findClassroomIdsByUser(userId, orgId)` no `ClassroomQueryPort` do curriculum,
  cruzada com `findClassroomIdsBySubject`. Disciplina sem turma vinculada não
  aparece para o aluno.
- `ListTopicsService` não tem checagem de matrícula (ao contrário de
  `ListSubjectContentsService`): qualquer aluno da org lê os tópicos de qualquer
  disciplina. Mesma correção.
- `SubjectListPage` já esconde criar/editar/excluir atrás de `canManage`, e
  `SubjectDetailPage` atrás de `canManage` — servem ao aluno em modo leitura sem
  alteração de permissão no front.
- `ListErrorState` (`ListErrorState.tsx:25`) tem texto fixo de conexão/sessão
  para qualquer erro; passa a receber o status e tratar 403 à parte. Usado
  também em turmas e membros, que herdam a correção.

## Scope

### In

- `GET /subjects` e `GET /subjects/{id}` acessíveis ao ALUNO, restritos às
  disciplinas vinculadas às turmas de que ele é membro.
- Checagem de matrícula em `GET /subjects/{id}/topics`.
- Item "Disciplinas" na sidebar para o papel ALUNO.
- `ListErrorState` com mensagem de permissão em 403, sem botão de retry.
- Nome da disciplina no cabeçalho da `SubjectDetailPage`.

### Out

- Gestão de conteúdo pelo aluno (criar/editar/excluir tópico ou material).
- Painel de disciplinas na página da turma e `GET /classrooms/{id}/subjects`.
- Vínculo disciplina↔turma pela UI — card separado (#feat-vinculo-disciplina).
- Refatoração do interceptor do axios, toasts globais e os demais 4xx sem
  feedback (card `erros-4xx-sem-feedback-na-ui`).

## Subtasks

- [x] Adicionar `findClassroomIdsByUser` ao `ClassroomQueryPort` do curriculum e implementar em `ClassroomQueryPortImpl`
- [x] Abrir `GET /subjects` ao ALUNO filtrando pelas turmas do aluno no `ListSubjectsService`
- [x] Abrir `GET /subjects/{id}` ao ALUNO com checagem de matrícula no `GetSubjectService` (403 `CONTENT_ACCESS_DENIED`)
- [x] Aplicar a checagem de matrícula do ALUNO no `ListTopicsService`
- [x] Exibir "Disciplinas" na sidebar para o papel ALUNO
- [x] Tratar 403 no `ListErrorState` com mensagem de permissão e sem botão de retry
- [ ] Exibir o nome da disciplina no cabeçalho da `SubjectDetailPage` via `useSubject`
