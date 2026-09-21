# Perguntas — fix: ações fora do papel visíveis na UI

- [x] **Mural para admin/gestor?** → **Esconder o mural para não-membros.**
  `ListAnnouncementsService` exige `isMember` independente do papel; admin/gestor
  não são membros da turma. A UI passa a refletir isso, sem tocar no backend.

- [x] **Como saber se o usuário leciona a disciplina?** → **Expor `teacherUserIds`
  no `SubjectResponse`.** Hoje o DTO traz só `teacherMemberIds` (ids de membro) e
  `GET /organizations/{id}/members` é ADMIN_ORG, então o frontend não consegue
  casar com o `userId` do JWT. É adição de dado, nenhuma regra de autorização muda.

- [x] **"Entrar via código": até onde levar?** → **Esconder o botão e bloquear a
  rota.** Observação de código: não existe rota dedicada de ingresso — o formulário
  é um card inline em `/classrooms`. O equivalente fiel é barrar o botão **e** a
  renderização do formulário para quem não é ALUNO.

- [x] **Como estruturar as checagens?** → **Helper/hook central reutilizável**,
  estendendo `apps/web/src/lib/roles.ts` (hoje só `canTeach`/`roleLabel`), com testes.
