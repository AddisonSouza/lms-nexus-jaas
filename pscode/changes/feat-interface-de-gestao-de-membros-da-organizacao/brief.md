# Interface de gestão de membros da organização (RF-06)

## Objetivo

Dar interface ao RF-06. O back-end de convites está pronto, mas nenhuma tela o
alcança — hoje só é possível convidar alguém chamando a API diretamente.

## Comportamento esperado

- `ADMIN_ORG` convida alguém por e-mail escolhendo o papel, pela interface.
- `ADMIN_ORG` vê a lista de membros da organização (nome, e-mail, papel).
- `ADMIN_ORG` remove um membro (o criador da organização não pode ser removido).

## Estado atual

O front só tem o lado de *receber* o convite (`AcceptInvitePage`, `InviteLinkForm`
do `/welcome`). `organization-api.ts` expõe apenas `createOrganization` e
`listOrganizations`; as rotas de organização são só `/organizations/new` e
`/organizations/:id`. Ninguém chama `POST /organizations/{id}/invitations` nem
`DELETE /organizations/{id}/members/{userId}`.

No back-end **não existe** endpoint para **listar** membros da organização
(`OrganizationMemberRepository` não tem busca por organização) nem para
**alterar o papel** de um membro — ambos são pré-requisitos da tela.

## Fora de escopo

- Convite por e-mail para turma e o fluxo de aceite reescrito (#77).
- Template de e-mail de convite (#73).
- Token de convite no cadastro (#130).

## Origem

Não é regressão: lacuna de implementação do RF-06, exposta pela revisão dos
fluxos de convite na #78.
