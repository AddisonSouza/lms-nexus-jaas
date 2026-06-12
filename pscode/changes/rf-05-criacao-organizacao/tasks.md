## 1. Infra — Flyway Migrations

- [ ] 1.1 [INFRA] Criar `V004__create_organizations_table.sql`: colunas `id, name, description, owner_id, created_at, updated_at, deleted_at`
- [ ] 1.2 [INFRA] Criar `V005__create_organization_members_table.sql`: colunas `id, organization_id, user_id, role, joined_at, deleted_at`, unique `(organization_id, user_id)`

## 2. Backend — Domain (módulo organization)

- [ ] 2.1 [BE] Criar `OrganizationId`, `Organization` (model) e `OrganizationMember` (model) em `domain/model/`
- [ ] 2.2 [BE] Criar enum `MemberRole` com valores `ADMIN_ORG, GESTOR, PROFESSOR, ALUNO`
- [ ] 2.3 [BE] Criar `OrganizationCreatedEvent` em `domain/event/`
- [ ] 2.4 [BE] Criar exceções: `OrganizationNameAlreadyExistsException`, `NotAnOrganizationMemberException`
- [ ] 2.5 [BE] Criar `CreateOrganizationUseCase` em `domain/port/in/`
- [ ] 2.6 [BE] Criar `OrganizationRepository` e `OrganizationMemberRepository` em `domain/port/out/`

## 3. Backend — Cross-Module Port (identity ← organization)

- [ ] 3.1 [BE] Criar `OrganizationMemberLookupPort` em `identity/domain/port/out/`: método `findRoleByUserAndOrg(userId, orgId) → Optional<String>`

## 4. Backend — Application (módulo organization)

- [ ] 4.1 [BE] Criar `CreateOrganizationCommand` em `application/dto/`
- [ ] 4.2 [BE] Criar `OrganizationResponse` em `application/dto/`
- [ ] 4.3 [BE] Criar `CreateOrganizationService` em `application/usecase/`: valida nome duplicado por owner, persiste org, persiste membro ADMIN_ORG, publica `OrganizationCreatedEvent`

## 5. Backend — Infrastructure (módulo organization)

- [ ] 5.1 [BE] Criar `OrganizationJpaEntity` e `OrganizationMemberJpaEntity` em `infrastructure/persistence/`
- [ ] 5.2 [BE] Criar `OrganizationRepositoryImpl` implementando `OrganizationRepository`
- [ ] 5.3 [BE] Criar `OrganizationMemberRepositoryImpl` implementando `OrganizationMemberRepository` **e** `OrganizationMemberLookupPort`

## 6. Backend — Identity Module (modificações)

- [ ] 6.1 [BE] Estender `JwtTokenService`: sobrecarga `generateAccessToken(userId, orgId, role)` que adiciona claims `org` e `roles` ao token
- [ ] 6.2 [BE] Estender `RefreshCommand` com campo `organizationId` (opcional)
- [ ] 6.3 [BE] Atualizar `RefreshService`: injetar `OrganizationMemberLookupPort`; quando `organizationId` presente, valida membership e chama `generateAccessToken(userId, orgId, role)`
- [ ] 6.4 [BE] Atualizar `RefreshRequest` DTO em `interfaces/rest/dto/` com campo `organizationId` opcional
- [ ] 6.5 [BE] Registrar `NotAnOrganizationMemberException → 403` no `GlobalExceptionMapper`

## 7. Backend — REST Interface (módulo organization)

- [ ] 7.1 [BE] Criar `CreateOrganizationRequest` em `interfaces/rest/dto/`: `@NotBlank String name`, `String description`
- [ ] 7.2 [BE] Criar `OrganizationResource`: `POST /organizations` — extrai `userId` do JWT (`@Context SecurityContext`), retorna 201 com `OrganizationResponseDto`

## 8. Backend — Testes

- [ ] 8.1 [BE] Teste unitário `CreateOrganizationServiceTest`: criação bem-sucedida, nome duplicado, evento publicado
- [ ] 8.2 [BE] Teste unitário `RefreshServiceTest`: refresh sem org (comportamento atual), com org válida, com org sem membership
- [ ] 8.3 [BE] Teste de integração `CreateOrganizationResourceIT` com `@QuarkusTest` + Testcontainers: fluxo register → confirm → create org → refresh com orgId

## 9. Frontend — API e Store

- [ ] 9.1 [FE] Criar `organization-api.ts` em `features/organization/api/`: função `createOrganization({name, description?})`
- [ ] 9.2 [FE] Adicionar `AUTH_KEYS.refreshWithOrg` em `query-keys.ts`
- [ ] 9.3 [FE] Estender `authStore` (Zustand) com campo `organizationId: string | null` e setter `setOrganization`
- [ ] 9.4 [FE] Criar schema Zod `createOrganizationSchema.ts`: `name` obrigatório (min 2, max 100), `description` opcional (max 500)

## 10. Frontend — Hooks

- [ ] 10.1 [FE] Criar `useCreateOrganization.ts`: mutation que chama `createOrganization`, em `onSuccess` chama refresh com `organizationId` e atualiza `authStore`
- [ ] 10.2 [FE] Atualizar `useRefresh.ts` (ou criar) para aceitar `organizationId` opcional no body do refresh

## 11. Frontend — Componentes e Rotas

- [ ] 11.1 [FE] Criar `CreateOrganizationForm.tsx`: campos `name` + `description`, botão "Criar Organização", estados loading/erro
- [ ] 11.2 [FE] Criar `CreateOrganizationPage.tsx`: wrapper com layout, usa `CreateOrganizationForm`
- [ ] 11.3 [FE] Adicionar rota `/organizations/new` em `routes.tsx` protegida por `ProtectedRoute`
- [ ] 11.4 [FE] Adicionar rota placeholder `/organizations/:id` em `routes.tsx`

## 12. Frontend — Testes

- [ ] 12.1 [FE] Teste `CreateOrganizationForm.test.tsx`: submissão bem-sucedida, validação nome vazio, erro 409
- [ ] 12.2 [FE] Teste `useCreateOrganization.test.ts`: mock de criação + refresh com orgId
