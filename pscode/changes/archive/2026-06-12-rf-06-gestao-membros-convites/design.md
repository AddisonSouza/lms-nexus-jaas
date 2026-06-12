## Context

Módulo `organization` já existe com `Organization`, `OrganizationMember`, `CreateOrganizationService` e `OrganizationResource`. Quarkus Mailer já está no pom. Redis está disponível para rate limiting. Precisamos adicionar o sub-fluxo de convites sem alterar a estrutura hexagonal existente.

## Goals / Non-Goals

**Goals:**
- Convidar membro por e-mail com papel (GESTOR, PROFESSOR, ALUNO)
- Aceitar convite via token (requer autenticação; sem conta → redirect para registro)
- Remover membro com soft delete (ADMIN_ORG criador protegido)
- Página frontend `/invitations/:token/accept`

**Non-Goals:**
- Alterar papel de membro existente
- Dashboard de listagem de membros
- Reenvio de convite expirado

## Decisions

### 1. Tabela `invitations` (Flyway V006)
```sql
CREATE TABLE invitations (
    id       CHAR(36) NOT NULL,
    organization_id CHAR(36) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL,
    token    CHAR(36)     NOT NULL UNIQUE,
    status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING',   -- PENDING | USED | EXPIRED
    invited_by CHAR(36)   NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_org    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_inv_inviter FOREIGN KEY (invited_by) REFERENCES users(id)
);
```
Sem soft delete — convites são imutáveis após uso.

### 2. Pacotes novos no módulo `organization`
```
domain/model/        Invitation.java, InvitationId.java
domain/model/        InvitationStatus.java (enum PENDING, USED, EXPIRED)
domain/event/        MemberInvitedEvent.java
domain/exception/    InvitationNotFoundException.java, InvitationExpiredException.java,
                     InvitationAlreadyUsedException.java, AlreadyAMemberException.java,
                     CannotRemoveOwnerException.java
domain/port/in/      InviteMemberUseCase.java, AcceptInviteUseCase.java, RemoveMemberUseCase.java
domain/port/out/     InvitationRepository.java

application/dto/     InviteMemberCommand.java, AcceptInviteCommand.java
application/usecase/ InviteMemberService.java, AcceptInviteService.java, RemoveMemberService.java

infrastructure/persistence/ InvitationJpaEntity.java, InvitationRepositoryImpl.java

interfaces/rest/dto/ InviteMemberRequest.java
interfaces/rest/     InvitationResource.java  (@Path("/invitations"))
```
`OrganizationResource` ganha endpoint `DELETE /organizations/{id}/members/{userId}`.

### 3. Proteção do endpoint de aceite
`POST /invitations/{token}/accept` requer JWT válido (sem org claim — o usuário ainda não tem org no token). O `sub` do JWT fornece o `userId`. Se não autenticado → 401; frontend redireciona para `/register?invite={token}`.

### 4. Validação de ADMIN_ORG nos endpoints de invite/remove
Injetar `JsonWebToken jwt` no resource e verificar `jwt.getClaim("org").equals(orgId)` + `jwt.getGroups().contains("ADMIN_ORG")`. Se falhar → 403.

### 5. E-mail de convite via Quarkus Mailer
`InviteMemberService` injeta `MailerPort` (port out existente no módulo identity, reutilizado via interface). Template simples com link `{baseUrl}/invitations/{token}/accept`.

### 6. Frontend: `features/invitation`
```
features/invitation/
  api/invitation-api.ts         getInvitation(token), acceptInvitation(token)
  hooks/useAcceptInvitation.ts  mutation: POST /invitations/{token}/accept
  components/
    AcceptInvitePage.tsx        ProtectedRoute; exibe org+papel; botão aceitar
```
Rota `/invitations/:token/accept` adicionada em `routes.tsx` com `ProtectedRoute`.

### 7. Redirect para registro
`AcceptInvitePage` detecta se não autenticado (via `useAuthStore`) antes de renderizar — se `!isAuthenticated` navega para `/register?invite={token}`. O `RegisterForm` já existente lê o query param `invite` para redirecionar de volta após o cadastro (out-of-scope alterar RegisterForm — o redirect pós-registro já leva para `/confirm-email` e depois `/login`, onde o usuário pode acessar o link novamente).

## Risks / Trade-offs

- **Convite para e-mail não cadastrado**: o fluxo de registro → confirm-email → login → aceite tem múltiplos passos. Simplificado aceitando que o usuário acessa o link novamente após login — não há persistência do token de convite no fluxo de registro neste RF.
- **Race condition no aceite**: dois aceites simultâneos do mesmo token. Mitigado com `status = USED` como campo único não-nulo e transação.
