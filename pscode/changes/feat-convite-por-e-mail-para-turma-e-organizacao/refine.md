# Painel de convites da organização

## Summary

Hoje o admin convida alguém e o convite some da tela até ser aceito. Esta
mudança mostra os convites na página de membros, com o estado de cada um
(pendente, aceito, expirado ou cancelado), e deixa o admin reenviar ou cancelar.
Entrar na turma continua sendo pelo código de 6 caracteres.

## Technical detail

- **Estado:** `InvitationStatus` ganha `CANCELLED`. A coluna já é `VARCHAR(20)`,
  então não precisa de migração. "Expirado" é calculado na leitura (`PENDING` +
  `expires_at` vencido), e o valor `EXPIRED` do enum continua sem ser gravado.
- **Listar:** `GET /organizations/{id}/invitations` (`ADMIN_ORG`, org do JWT).
  Retorna e-mail, papel, estado, quem convidou, `createdAt` e `expiresAt`, do mais
  recente para o mais antigo. Não retorna o token, porque o link é secreto.
- **Cancelar:** `DELETE /organizations/{id}/invitations/{invitationId}` → 204.
  Só convite `PENDING` pode ser cancelado; nos outros estados a resposta é
  409 `INVITATION_NOT_PENDING`.
- **Reenviar = reconvidar:** o `InviteMemberService` cancela os convites pendentes
  do mesmo e-mail na organização antes de emitir o novo token e o e-mail. Na tela,
  "Reenviar" chama o `POST /invitations` que já existe.
- **Aceite de convite cancelado:** 410 `INVITATION_CANCELLED` (hoje responderia
  409 `ALREADY_USED`). A `AcceptInvitePage` mostra uma mensagem própria.
- **FE:** seção "Convites" na `OrganizationMembersPage`, com TanStack Query e
  resposta validada por Zod. Reenviar/cancelar invalidam a query da lista.

## Scope

### In

- Estado `CANCELLED`, listagem, cancelamento e reconvite que substitui o pendente.
- Seção de convites na página de membros (estado, reenviar, cancelar).
- Mensagem de convite cancelado na tela de aceite.
- `API_CONTRACT.md` (RF-06) e o card retitulado para refletir o escopo.

### Out

- Convite por e-mail para turma. RN-05 e RF-08 ficam intactos.
- Remover o código de 6 caracteres ou o campo de colar o link no `/welcome`.
- Job que grava `EXPIRED`, convite em massa/CSV, template de e-mail (#73).
- Mudanças em papéis/permissões e notificações in-app.

## Subtasks

- [x] BE: estado `CANCELLED` e reconvite que cancela o pendente anterior + testes
- [x] BE: `GET /organizations/{id}/invitations` com estado calculado + testes
- [x] BE: `DELETE /organizations/{id}/invitations/{invitationId}` + aceite de cancelado → 410 + testes
- [x] Docs: endpoints e erros novos no `API_CONTRACT.md` (RF-06)
- [x] FE: seção de convites na página de membros (lista + estado)
- [ ] FE: ações reenviar e cancelar + mensagem de convite cancelado no aceite
- [ ] Validar o golden path na aplicação (convidar → reenviar → link antigo recusado → cancelar)
