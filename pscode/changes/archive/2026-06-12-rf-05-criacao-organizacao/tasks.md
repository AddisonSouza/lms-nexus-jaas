## 1. Infra — Flyway Migrations

- [x] 1.1 [INFRA] Criar `V004__create_organizations_table.sql`: colunas `id, name, description, owner_id, created_at, updated_at, deleted_at`
- [x] 1.2 [INFRA] Criar `V005__create_organization_members_table.sql`: colunas `id, organization_id, user_id, role, joined_at, deleted_at`, unique `(organization_id, user_id)`

## 2. Backend — Domain (módulo organization)

- [x] 2.1 [BE] Criar `OrganizationId`, `Organization` (model) e `OrganizationMember` (model) em `domain/model/`
- [x] 2.2 [BE] Criar enum `MemberRole` com valores `ADMIN_ORG, GESTOR, PROFESSOR, ALUNO`
- [x] 2.3 [BE] Criar `OrganizationCreatedEvent` em `domain/event/`
- [x] 2.4 [BE] Criar exceções: `OrganizationNameAlreadyExistsException`, `NotAnOrganizationMemberException`
- [x] 2.5 [BE] Criar `CreateOrganizationUseCase` em `domain/port/in/`
- [x] 2.6 [BE] Criar `OrganizationRepository` e `OrganizationMemberRepository` em `domain/port/out/`

## 3. Backend — Cross-Module Port (identity ← organization)

- [x] 3.1 [BE] Criar `OrganizationMemberLookupPort` em `identity/domain/port/out/`: método `findRoleByUserAndOrg(userId, orgId) → Optional<String>`

## 4. Backend — Application (módulo organization)

- [x] 4.1 [BE] Criar `CreateOrganizationCommand` em `application/dto/`
- [x] 4.2 [BE] Criar `OrganizationResponse` em `application/dto/`
- [x] 4.3 [BE] Criar `CreateOrganizationService` em `application/usecase/`: valida nome duplicado por owner, persiste org, persiste membro ADMIN_ORG, publica `OrganizationCreatedEvent`

## 5. Backend — Infrastructure (módulo organization)

- [x] 5.1 [BE] Criar `OrganizationJpaEntity` e `OrganizationMemberJpaEntity` em `infrastructure/persistence/`
- [x] 5.2 [BE] Criar `OrganizationRepositoryImpl` implementando `OrganizationRepository`
- [x] 5.3 [BE] Criar `OrganizationMemberRepositoryImpl` implementando `OrganizationMemberRepository` **e** `OrganizationMemberLookupPort`

## 6. Backend — Identity Module (modificações)

- [x] 6.1 [BE] Estender `JwtTokenService`: sobrecarga `generateAccessToken(userId, orgId, role)` que adiciona claims `org` e `roles` ao token
- [x] 6.2 [BE] Estender `RefreshCommand` com campo `organizationId` (opcional)
- [x] 6.3 [BE] Atualizar `RefreshService`: injetar `OrganizationMemberLookupPort`; quando `organizationId` presente, valida membership e chama `generateAccessToken(userId, orgId, role)`
- [x] 6.4 [BE] Atualizar `RefreshRequest` DTO em `interfaces/rest/dto/` com campo `organizationId` opcional
- [x] 6.5 [BE] Registrar `NotAnOrganizationMemberException → 403` no `GlobalExceptionMapper`

## 7. Backend — REST Interface (módulo organization)

- [x] 7.1 [BE] Criar `CreateOrganizationRequest` em `interfaces/rest/dto/`: `@NotBlank String name`, `String description`
- [x] 7.2 [BE] Criar `OrganizationResource`: `POST /organizations` — extrai `userId` do JWT (`@Context SecurityContext`), retorna 201 com `OrganizationResponseDto`

## 8. Backend — Testes

- [x] 8.1 [BE] Teste unitário `CreateOrganizationServiceTest`: criação bem-sucedida, nome duplicado, evento publicado
- [x] 8.2 [BE] Teste unitário `RefreshServiceTest`: refresh sem org (comportamento atual), com org válida, com org sem membership
- [x] 8.3 [BE] Teste de integração `CreateOrganizationResourceIT` com `@QuarkusTest` + Testcontainers: fluxo register → confirm → create org → refresh com orgId

## 9. Frontend — API e Store

- [x] 9.1 [FE] Criar `organization-api.ts` em `features/organization/api/`: função `createOrganization({name, description?})`
- [x] 9.2 [FE] Adicionar `AUTH_KEYS.refreshWithOrg` em `query-keys.ts`
- [x] 9.3 [FE] Estender `authStore` (Zustand) com campo `organizationId: string | null` e setter `setOrganization`
- [x] 9.4 [FE] Criar schema Zod `createOrganizationSchema.ts`: `name` obrigatório (min 2, max 100), `description` opcional (max 500)

## 10. Frontend — Hooks

- [x] 10.1 [FE] Criar `useCreateOrganization.ts`: mutation que chama `createOrganization`, em `onSuccess` chama refresh com `organizationId` e atualiza `authStore`
- [x] 10.2 [FE] Atualizar `useRefresh.ts` (ou criar) para aceitar `organizationId` opcional no body do refresh

## 11. Frontend — Componentes e Rotas

- [x] 11.1 [FE] Criar `CreateOrganizationForm.tsx`: campos `name` + `description`, botão "Criar Organização", estados loading/erro
- [x] 11.2 [FE] Criar `CreateOrganizationPage.tsx`: wrapper com layout, usa `CreateOrganizationForm`
- [x] 11.3 [FE] Adicionar rota `/organizations/new` em `routes.tsx` protegida por `ProtectedRoute`
- [x] 11.4 [FE] Adicionar rota placeholder `/organizations/:id` em `routes.tsx`

## 12. Frontend — Testes

- [x] 12.1 [FE] Teste `CreateOrganizationForm.test.tsx`: submissão bem-sucedida, validação nome vazio, erro 409
- [x] 12.2 [FE] Teste `useCreateOrganization.test.ts`: mock de criação + refresh com orgId
