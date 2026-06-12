## 1. Infra — Flyway Migration

- [ ] 1.1 [INFRA] Criar `V006__create_invitations_table.sql`: colunas `id, organization_id, email, role, token (UNIQUE), status, invited_by, expires_at, created_at`; FKs para `organizations` e `users`

## 2. Backend — Domain

- [ ] 2.1 [BE] Criar `InvitationId`, `Invitation` (model) e enum `InvitationStatus` (`PENDING, USED, EXPIRED`) em `domain/model/`
- [ ] 2.2 [BE] Criar `MemberInvitedEvent` em `domain/event/`
- [ ] 2.3 [BE] Criar exceções: `InvitationNotFoundException`, `InvitationExpiredException`, `InvitationAlreadyUsedException`, `AlreadyAMemberException`, `CannotRemoveOwnerException`
- [ ] 2.4 [BE] Criar ports de entrada: `InviteMemberUseCase`, `AcceptInviteUseCase`, `RemoveMemberUseCase` em `domain/port/in/`
- [ ] 2.5 [BE] Criar `InvitationRepository` em `domain/port/out/`: `save`, `findByToken`, `findActiveByOrgAndEmail`

## 3. Backend — Application

- [ ] 3.1 [BE] Criar `InviteMemberCommand` e `AcceptInviteCommand` em `application/dto/`
- [ ] 3.2 [BE] Criar `InviteMemberService`: valida membership duplicada, gera token UUID, persiste convite, publica `MemberInvitedEvent`
- [ ] 3.3 [BE] Criar `AcceptInviteService`: valida token (existência, status PENDING, TTL), cria `OrganizationMember`, marca token como USED
- [ ] 3.4 [BE] Criar `RemoveMemberService`: valida que userId não é owner, aplica soft delete em `organization_members`

## 4. Backend — Infrastructure

- [ ] 4.1 [BE] Criar `InvitationJpaEntity` em `infrastructure/persistence/` com `@PrePersist` para `created_at`
- [ ] 4.2 [BE] Criar `InvitationRepositoryImpl` implementando `InvitationRepository`
- [ ] 4.3 [BE] Criar `InvitationMailService` em `infrastructure/mail/`: envia e-mail de convite via Quarkus Mailer com link `{baseUrl}/invitations/{token}/accept`

## 5. Backend — REST

- [ ] 5.1 [BE] Criar `InviteMemberRequest` em `interfaces/rest/dto/`: `@Email String email`, `@NotNull MemberRole role`
- [ ] 5.2 [BE] Criar `InvitationResource` com `POST /organizations/{id}/invitations` (requer `org` claim + `ADMIN_ORG`) e `POST /invitations/{token}/accept` (requer JWT)
- [ ] 5.3 [BE] Adicionar `DELETE /organizations/{id}/members/{userId}` em `OrganizationResource` (requer `org` claim + `ADMIN_ORG`)
- [ ] 5.4 [BE] Registrar exceções novas no `GlobalExceptionMapper`: `InvitationNotFoundException→404`, `InvitationExpiredException→410`, `InvitationAlreadyUsedException→409`, `AlreadyAMemberException→409`, `CannotRemoveOwnerException→403`

## 6. Backend — Testes

- [ ] 6.1 [BE] Teste unitário `InviteMemberServiceTest`: convite bem-sucedido, e-mail já membro, evento publicado
- [ ] 6.2 [BE] Teste unitário `AcceptInviteServiceTest`: aceite OK, token expirado, token USED, usuário já membro
- [ ] 6.3 [BE] Teste unitário `RemoveMemberServiceTest`: remoção OK, tentativa de remover owner
- [ ] 6.4 [BE] Teste de integração `InvitationResourceIT`: `POST /organizations/{id}/invitations` (201, 409 duplicado, 403 sem role), `POST /invitations/{token}/accept` (204, 410, 409)

## 7. Frontend — API e Hooks

- [ ] 7.1 [FE] Criar `features/invitation/api/invitation-api.ts`: `getInvitationInfo(token)` e `acceptInvitation(token)`
- [ ] 7.2 [FE] Criar `features/invitation/hooks/useAcceptInvitation.ts`: mutation `acceptInvitation`, em `onSuccess` navega para `/organizations/{orgId}`

## 8. Frontend — Componentes e Rotas

- [ ] 8.1 [FE] Criar `AcceptInvitePage.tsx`: verifica autenticação (se não auth → redirect `/register?invite={token}`); exibe org e papel do convite; botão "Aceitar Convite"
- [ ] 8.2 [FE] Adicionar rota `/invitations/:token/accept` em `routes.tsx` (sem ProtectedRoute — a página gerencia o redirect internamente)

## 9. Frontend — Testes

- [ ] 9.1 [FE] Teste `AcceptInvitePage.test.tsx`: usuário autenticado vê página e aceita; usuário não autenticado é redirecionado para `/register?invite=TOKEN`
- [ ] 9.2 [FE] Teste `useAcceptInvitation.test.ts`: aceite bem-sucedido navega para org; erro 410 exposto
