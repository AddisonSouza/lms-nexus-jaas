# Interface de gestão de membros da organização (RF-06) — Delta

## Added

- **Listar membros da organização.** `GET /organizations/{id}/members` devolve os
  membros ativos, ordenados por nome, com `id`, `userId`, `name`, `email`, `role`,
  `joinedAt` e `owner`. Nome e e-mail vêm do módulo `identity` pelo
  `UserDirectoryPort`; membro sem perfil resolvido vem com `name`/`email` nulos e
  aparece por último, em vez de derrubar a listagem. Vínculos com soft delete não
  aparecem. Exige `ADMIN_ORG` da própria organização (claim `org` = `{id}`).
- **Alterar o papel de um membro.** `PATCH /organizations/{id}/members/{userId}`
  com `{ "role": "GESTOR | PROFESSOR | ALUNO" }` → `204`. Recusa alterar o papel
  do criador (`403 CANNOT_CHANGE_OWNER_ROLE`) e recusa atribuir `ADMIN_ORG`
  (`422 ROLE_NOT_ASSIGNABLE`). O papel novo só vale no próximo login ou troca de
  organização — o papel viaja no JWT, que não é reemitido aqui.
- **Tela `/organizations/:id/members`.** O `ADMIN_ORG` vê os membros, convida por
  e-mail escolhendo o papel, altera o papel pela própria tabela e remove um
  membro com confirmação. Alcançada pela seção "Organização" da sidebar, visível
  só para `ADMIN_ORG`; a rota exige o mesmo papel.
- **Criador protegido na interface.** A linha do criador não oferece seletor de
  papel nem ação de remover — as ações que o back-end recusaria não são exibidas.
  Um membro com papel não atribuível (`ADMIN_ORG` sem ser o criador) aparece com
  badge em vez de um seletor que mostraria o papel errado.

## Changed

- **`DELETE /organizations/{id}/members/{userId}` — contrato corrigido, não o
  código.** O `API_CONTRACT.md` prometia `422` ao remover o criador, mas
  `CannotRemoveOwnerException` sempre respondeu `403`. Documentado o `403` real e
  acrescentado o `404` que já era possível e não constava.
- **`InvitationResourceIT.accept_validToken_returns204`** passou a autenticar como
  o convidado. Autenticava como o convidante e falhava desde a regra de
  casamento de e-mail introduzida na #138.

## Notes

Fora deste delta, como decidido no refinamento: listar/reenviar/cancelar convites
pendentes, acesso do `GESTOR` à tela e promover alguém a `ADMIN_ORG` pela
interface.
