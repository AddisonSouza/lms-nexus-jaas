# Código fixo de turma visível na listagem — Delta

## Added

- **`classroom-management` / Invite code visibility**: regra única de
  visibilidade do código de convite — `ADMIN_ORG`, `GESTOR` e `PROFESSOR`
  recebem o `inviteCode` na listagem e no detalhe; `ALUNO` recebe
  `inviteCode: null`. O aluno entra pelo código, mas nunca o recebe de volta.
- **Coluna "Código" em `ClassroomListPage`**: exibida em fonte mono com botão
  copiar, renderizada apenas para os papéis que recebem o código.
- **`classroom-join-by-code` / Immutable invite code**: o código é gerado uma
  única vez na criação e sobrevive a qualquer update da turma.

## Changed

- **`GET /classrooms`**: antes devolvia o `inviteCode` para todos os papéis,
  inclusive `ALUNO`. Agora aplica a regra de visibilidade a partir do
  `requesterRole`.
- **`GET /classrooms/{id}`**: antes escondia o código de `PROFESSOR` junto com
  `ALUNO`. Agora o professor recebe o código; só o aluno não.
- **`Classroom.inviteCode`**: passa a ser `final`, documentando a imutabilidade
  no domínio.

## Removed

- **`POST /classrooms/{id}/invite-code/regenerate`** e todo o caminho de
  regeneração: `RegenerateInviteCodeUseCase`, `RegenerateInviteCodeService` e
  seus testes no back-end; `useRegenerateInviteCode`, `regenerateInviteCode` e o
  botão de regenerar do `ClassroomDetailPage` no front-end.
- **`classroom-join-by-code` / Regenerate invite code**: requisito e seus três
  cenários, substituídos por "Immutable invite code".

## Notes

- Sem migration — nenhuma mudança de schema; turmas existentes mantêm o código.
- Fora de escopo e inalterados: formato do código (6 chars, `InviteCode.generate()`),
  o fluxo `POST /classrooms/join` e a resposta que devolve o código a quem acabou
  de digitá-lo.
