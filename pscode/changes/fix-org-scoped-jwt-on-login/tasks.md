## 1. Backend — Domínio e Port

- [ ] 1.1 [BE] Criar `OrgMembership` (record: `organizationId`, `role`) em `identity/domain/model/`
- [ ] 1.2 [BE] Adicionar `List<OrgMembership> findOrganizationsByUser(String userId)` à porta `OrganizationMemberLookupPort` (`identity/domain/port/out/`)

## 2. Backend — Adapter

- [ ] 2.1 [BE] Implementar `findOrganizationsByUser` em `OrganizationMemberRepositoryImpl` (módulo `organization`) — JPQL contra `OrganizationMemberJpaEntity` filtrando `userId` e `deletedAt IS NULL`

## 3. Backend — Use Cases

- [ ] 3.1 [BE] Atualizar `AuthenticateService`: após validar credenciais, chamar `findOrganizationsByUser`; se exatamente 1 resultado, gerar token com `generateAccessToken(userId, orgId, role)`; caso contrário, manter `generateAccessToken(userId)`
- [ ] 3.2 [BE] Atualizar `RefreshTokenService`: no ramo onde `command.organizationId()` é nulo/vazio, aplicar a mesma resolução antes de decidir entre `generateAccessToken(userId, orgId, role)` e `generateAccessToken(userId)`

## 4. Backend — Testes

- [ ] 4.1 [BE] Testes unitários de `AuthenticateService` cobrindo: usuário com 1 organização (token com `org`/`role`), usuário sem organização (token sem claims), usuário com 2+ organizações (token sem claims)
- [ ] 4.2 [BE] Testes unitários de `RefreshTokenService` cobrindo os mesmos três cenários no ramo sem `organizationId`, e confirmando que o ramo com `organizationId` explícito não muda de comportamento
- [ ] 4.3 [BE] Teste de integração (`@QuarkusTest`) de `findOrganizationsByUser` em `OrganizationMemberRepositoryImpl` (0, 1 e 2 organizações; membership com `deleted_at` preenchido é ignorado)
- [ ] 4.4 [BE] Teste de integração `@QuarkusTest` para `POST /auth/login` e `POST /auth/refresh`: usuário com exatamente 1 organização recebe token decodificável com `org`/`roles` corretos
