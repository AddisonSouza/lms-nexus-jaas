## 1. Infra — Flyway Migration

- [x] 1.1 [INFRA] Criar `V006__create_invitations_table.sql`: colunas `id, organization_id, email, role, token (UNIQUE), status, invited_by, expires_at, created_at`; FKs para `organizations` e `users`

## 2. Backend — Domain

- [x] 2.1 [BE] Criar `InvitationId`, `Invitation` (model) e enum `InvitationStatus` (`PENDING, USED, EXPIRED`) em `domain/model/`
- [x] 2.2 [BE] Criar `MemberInvitedEvent` em `domain/event/`
- [x] 2.3 [BE] Criar exceções: `InvitationNotFoundException`, `InvitationExpiredException`, `InvitationAlreadyUsedException`, `AlreadyAMemberException`, `CannotRemoveOwnerException`
- [x] 2.4 [BE] Criar ports de entrada: `InviteMemberUseCase`, `AcceptInviteUseCase`, `RemoveMemberUseCase` em `domain/port/in/`
- [x] 2.5 [BE] Criar `InvitationRepository` em `domain/port/out/`: `save`, `findByToken`, `findActiveByOrgAndEmail`

## 3. Backend — Application

- [x] 3.1 [BE] Criar `InviteMemberCommand` e `AcceptInviteCommand` em `application/dto/`
- [x] 3.2 [BE] Criar `InviteMemberService`: valida membership duplicada, gera token UUID, persiste convite, publica `MemberInvitedEvent`
- [x] 3.3 [BE] Criar `AcceptInviteService`: valida token (existência, status PENDING, TTL), cria `OrganizationMember`, marca token como USED
- [x] 3.4 [BE] Criar `RemoveMemberService`: valida que userId não é owner, aplica soft delete em `organization_members`

## 4. Backend — Infrastructure

- [x] 4.1 [BE] Criar `InvitationJpaEntity` em `infrastructure/persistence/` com `@PrePersist` para `created_at`
- [x] 4.2 [BE] Criar `InvitationRepositoryImpl` implementando `InvitationRepository`
- [x] 4.3 [BE] Criar `InvitationMailService` em `infrastructure/mail/`: envia e-mail de convite via Quarkus Mailer com link `{baseUrl}/invitations/{token}/accept`

## 5. Backend — REST

- [x] 5.1 [BE] Criar `InviteMemberRequest` em `interfaces/rest/dto/`: `@Email String email`, `@NotNull MemberRole role`
- [x] 5.2 [BE] Criar `InvitationResource` com `POST /organizations/{id}/invitations` (requer `org` claim + `ADMIN_ORG`) e `POST /invitations/{token}/accept` (requer JWT)
- [x] 5.3 [BE] Adicionar `DELETE /organizations/{id}/members/{userId}` em `OrganizationResource` (requer `org` claim + `ADMIN_ORG`)
- [x] 5.4 [BE] Registrar exceções novas no `GlobalExceptionMapper`: `InvitationNotFoundException→404`, `InvitationExpiredException→410`, `InvitationAlreadyUsedException→409`, `AlreadyAMemberException→409`, `CannotRemoveOwnerException→403`

## 6. Backend — Testes

- [x] 6.1 [BE] Teste unitário `InviteMemberServiceTest`: convite bem-sucedido, e-mail já membro, evento publicado
- [x] 6.2 [BE] Teste unitário `AcceptInviteServiceTest`: aceite OK, token expirado, token USED, usuário já membro
- [x] 6.3 [BE] Teste unitário `RemoveMemberServiceTest`: remoção OK, tentativa de remover owner
- [x] 6.4 [BE] Teste de integração `InvitationResourceIT`: `POST /organizations/{id}/invitations` (201, 409 duplicado, 403 sem role), `POST /invitations/{token}/accept` (204, 410, 409)

## 7. Frontend — API e Hooks

- [x] 7.1 [FE] Criar `features/invitation/api/invitation-api.ts`: `getInvitationInfo(token)` e `acceptInvitation(token)`
- [x] 7.2 [FE] Criar `features/invitation/hooks/useAcceptInvitation.ts`: mutation `acceptInvitation`, em `onSuccess` navega para `/organizations/{orgId}`

## 8. Frontend — Componentes e Rotas

- [x] 8.1 [FE] Criar `AcceptInvitePage.tsx`: verifica autenticação (se não auth → redirect `/register?invite={token}`); exibe org e papel do convite; botão "Aceitar Convite"
- [x] 8.2 [FE] Adicionar rota `/invitations/:token/accept` em `routes.tsx` (sem ProtectedRoute — a página gerencia o redirect internamente)

## 9. Frontend — Testes

- [x] 9.1 [FE] Teste `AcceptInvitePage.test.tsx`: usuário autenticado vê página e aceita; usuário não autenticado é redirecionado para `/register?invite=TOKEN`
- [x] 9.2 [FE] Teste `useAcceptInvitation.test.ts`: aceite bem-sucedido navega para org; erro 410 exposto
