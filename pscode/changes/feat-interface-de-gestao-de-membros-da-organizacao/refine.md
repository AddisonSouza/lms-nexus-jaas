# Interface de gestão de membros da organização (RF-06)

## Summary

Hoje não existe tela para gerir quem faz parte da organização: convidar alguém
só é possível chamando a API na mão. Esta mudança cria a página **Membros**,
onde o `ADMIN_ORG` vê quem está na organização, convida por e-mail escolhendo o
papel, altera o papel de um membro e remove quem saiu. Para isso, dois endpoints
que faltavam no back-end também entram.

## Technical detail

**Back-end (`module/organization`)**

- **Listar membros não existe.** `OrganizationMemberRepository` não tem busca por
  organização — é preciso `findActiveMembersByOrganization` (JPQL com
  `deletedAt IS NULL`, ordenado por nome).
- Nome e e-mail vêm do módulo `identity` pelo `UserDirectoryPort`, que hoje só
  expõe `findEmailById`. Estender com uma busca em lote (id → nome + e-mail),
  no padrão do `UserDirectoryPort` do `classroom` (`findNamesByIds`).
- A resposta traz `owner: true` para o criador, para a UI desabilitar remover e
  alterar papel sem precisar de outra chamada.
- **Alterar papel não existe.** Novo `ChangeMemberRoleUseCase`. Cuidado: não
  reaproveitar `save()` — ele faz `em.merge` de uma entidade nova e zeraria
  `joined_at`; usar um `updateRole(memberId, role)` com JPQL `UPDATE`.
- Regras: owner não muda de papel nem é removido (`CannotRemoveOwnerException`);
  papel válido ∈ `GESTOR`, `PROFESSOR`, `ALUNO`.
- Ambos os endpoints repetem o guard já usado no `OrganizationResource`: claim
  `org` do JWT igual ao `{id}` do path **e** grupo `ADMIN_ORG`.
- Sem mudança de schema — `organization_members` já tem `role`, `joined_at` e
  `deleted_at`. Nenhuma migration Flyway.

**Front-end (`features/organization`)**

- `organization-api.ts` ganha `listMembers`, `inviteMember`, `removeMember` e
  `changeMemberRole`, cada resposta validada com Zod; `query-keys.ts` ganha
  `members(orgId)`.
- Página nova em rota protegida (`ProtectedRoute roles={['ADMIN_ORG']}`) dentro
  de `RequireOrganization`; o item na sidebar só aparece para `ADMIN_ORG`.
- Tabela e diálogo de remoção seguem `ClassroomMembersPanel` + `ConfirmDialog`.
  Server state em TanStack Query, formulário com Zod, ícones Lucide.

## Scope

### In

- `GET /organizations/{id}/members` e `PATCH /organizations/{id}/members/{userId}`.
- Página `/organizations/:id/members`: listar, convidar, alterar papel, remover.
- Item "Membros" na sidebar para `ADMIN_ORG`.
- Atualização do `API_CONTRACT.md` (RF-06).

### Out

- Listar, reenviar ou cancelar convites pendentes.
- Acesso de `GESTOR` à tela (só `ADMIN_ORG`).
- Promover alguém a `ADMIN_ORG` pela interface.
- Convite por e-mail para turma e reescrita do aceite (#77), template de e-mail
  (#73), token de convite no cadastro (#130).

## Subtasks

- [x] BE: endpoint `GET /organizations/{id}/members` (repositório, UserDirectoryPort em lote, use case, resposta com `owner`) + testes
- [x] BE: endpoint `PATCH /organizations/{id}/members/{userId}` para alterar papel (owner protegido, `updateRole` por JPQL) + testes
- [x] Docs: registrar os dois endpoints no `API_CONTRACT.md` (RF-06)
- [x] FE: camada de dados — `listMembers`/`inviteMember`/`removeMember`/`changeMemberRole` com Zod, query keys e hooks TanStack Query
- [x] FE: página `OrganizationMembersPage` com a tabela de membros, remoção via `ConfirmDialog`, rota protegida e item "Membros" na sidebar
- [x] FE: `InviteMemberDialog` — formulário Zod (e-mail + papel) ligado ao `POST /invitations`, tratando o 409 de membro já existente
- [x] FE: alteração de papel inline na tabela, desabilitada para o owner
- [x] Validar o golden path na aplicação (convidar → listar → alterar papel → remover)
