# Painel de convites da organização — Delta

## Added

- **Estado `CANCELLED`** no convite. "Expirado" é calculado na leitura
  (`Invitation.effectiveStatus()`: `PENDING` com `expiresAt` vencido sai como
  `EXPIRED`); o banco nunca grava `EXPIRED` e não há job nem migração.
- **`GET /organizations/{id}/invitations`** (`ADMIN_ORG` da org do JWT): todos os
  convites, do mais recente para o mais antigo, com estado efetivo e o nome de
  quem convidou. **Não devolve o token** — o link é segredo do convidado.
- **`DELETE /organizations/{id}/invitations/{invitationId}`**: cancela um convite
  pendente. `409 INVITATION_NOT_PENDING` para aceito, cancelado ou expirado;
  `404 INVITATION_NOT_FOUND` para convite de outra organização, sem revelar que
  existe.
- **Seção "Convites" na página de membros**: e-mail, papel, estado, convidante e
  data. Reenviar em pendente, expirado e cancelado; cancelar só em pendente, com
  confirmação. Falha reportada na própria linha.
- Testes: `ListOrganizationInvitationsServiceTest`, `CancelInvitationServiceTest`,
  `ListOrganizationInvitationsResourceIT`, `CancelInvitationResourceIT`, casos
  novos em `InviteMemberServiceTest`, `AcceptInviteServiceTest`,
  `InvitationResourceIT` e nos testes de `OrganizationMembersPage`, hooks e
  `AcceptInvitePage`.

## Changed

- **Reconvidar é reenviar.** `InviteMemberService` agora cancela, na mesma
  transação, o convite pendente e não expirado do mesmo e-mail na organização
  antes de emitir o novo token. Antes, cada convite criava mais um link válido.
- **Aceitar convite cancelado responde `410 INVITATION_CANCELLED`** (antes caía em
  `409 INVITATION_ALREADY_USED`). A `AcceptInvitePage` distingue pelo código:
  "cancelado pelo administrador" em vez de "expirou".
- Convidar invalida também a lista de convites, e o aviso deixou de dizer que o
  convite "aparece na lista depois de aceitar".
- `API_CONTRACT.md` (RF-06): endpoints, reconvite e o novo 410.

## Unchanged

- **Turma continua entrando por código de 6 caracteres** — RN-05 e RF-08
  intactos. O card nasceu pedindo convite por e-mail para turma; o refine tirou
  isso do escopo.
- A prévia `GET /invitations/{token}` segue sem olhar o estado: o convite
  cancelado só é recusado no aceite.

## Reported, not fixed

- **#198** — login por `/login?invite=` cai em `/welcome`: o `PublicRoute`
  redireciona para `/` antes do `useLogin` levar ao aceite. Achado no golden path.
- O aviso "Convite enviado para …" permanece na tela depois de cancelar (visual).
