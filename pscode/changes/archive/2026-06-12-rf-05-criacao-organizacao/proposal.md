## Why

RF-05 é o ponto de entrada para o modelo multi-tenant do LMS: sem uma organização, nenhum outro RF (turmas, convites, currículos) pode existir. O módulo `organization` precisa ser criado do zero. Adicionalmente, o JWT atual só carrega `sub` (userId) — para que RF-06+ possam extrair `organization_id` do token (DB-MT-03), o refresh endpoint precisa ser estendido para emitir tokens com claim `org`. Módulos: `organization` (novo) e `identity` (modificado).

## What Changes

- Novo módulo `organization`: `POST /organizations` cria a org e vincula o criador como `ADMIN_ORG` em `organization_members`; `OrganizationCreatedEvent` publicado via CDI
- Migrations V004 (`organizations`) e V005 (`organization_members`) — colunas obrigatórias: `organization_id`, `name`, `deleted_at` (soft delete)
- `JwtTokenService` estendido: `generateAccessToken(userId, orgId?)` — sem `orgId` emite token sem claim `org` (compatível com fluxo atual); com `orgId` valida membership e inclui claim `org`
- Refresh endpoint (`POST /auth/refresh`) aceita `organizationId` opcional no body; se presente, valida membership via `OrganizationMemberRepository` e emite token contextualizado
- Frontend: nova feature `features/organization` com `CreateOrganizationForm` e hook `useCreateOrganization`; após criação chama refresh com o novo `organizationId`

## Capabilities

### New Capabilities

- `organization-creation`: Criação de organização educacional e vínculo automático do criador como ADMIN_ORG

### Modified Capabilities

- `authentication`: Refresh endpoint passa a aceitar `organizationId` opcional para emitir JWT com claim `org`

## Non-goals

- Upload de logotipo (StoragePort não implementado)
- Listagem ou edição de organizações
- Gestão de membros (RF-06)
- Dashboard da organização (RF-17)

## Impact

- **Backend:** novo módulo `organization` (domain, application, infrastructure, interfaces); `identity` modificado (JwtTokenService, RefreshService, RefreshResource)
- **Frontend:** nova feature `features/organization`; `authStore` estendido com `organizationId`; `useRefresh` / refresh pós-criação
- **Infra:** Flyway V004 + V005
