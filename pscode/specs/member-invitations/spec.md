### REQ-INV-01 — Convidar membro por e-mail
`ADMIN_ORG` pode convidar um usuário por e-mail informando o papel (GESTOR, PROFESSOR ou ALUNO). O sistema persiste o convite em `invitations` com token UUID único, `expires_at = now + 7 dias` e status `PENDING`. E-mail enviado com link `{baseUrl}/invitations/{token}/accept`.

#### Scenario: Convite bem-sucedido
- **WHEN** `POST /organizations/{id}/invitations` com JWT `org=orgId, roles=[ADMIN_ORG]`, body `{email, role}`
- **THEN** 201; `invitations` tem nova linha `{token, orgId, email, role, PENDING, expires_at}`; e-mail enviado ao destinatário

#### Scenario: E-mail já é membro ativo
- **WHEN** convite para e-mail que já consta em `organization_members` com `deleted_at IS NULL`
- **THEN** 409 `{"error": "ALREADY_A_MEMBER"}`

#### Scenario: Papel inválido
- **WHEN** `role` enviado não é GESTOR, PROFESSOR ou ALUNO
- **THEN** 422

#### Scenario: Sem JWT de organização
- **WHEN** JWT sem claim `org` ou sem role `ADMIN_ORG`
- **THEN** 403

---

### REQ-INV-02 — Aceitar convite
Usuário autenticado aceita convite via token. O sistema valida token (existência, status `PENDING`, não expirado) e cria entrada em `organization_members` com o papel do convite. Token marcado como `USED`.

#### Scenario: Aceite bem-sucedido
- **WHEN** `POST /invitations/{token}/accept` com JWT válido e token `PENDING` não expirado
- **THEN** 204; `organization_members` tem nova linha `{userId, orgId, role}`; token marcado `USED`

#### Scenario: Token expirado
- **WHEN** token com `expires_at < now`
- **THEN** 410 `{"error": "INVITATION_EXPIRED"}`

#### Scenario: Token já utilizado
- **WHEN** token com status `USED`
- **THEN** 409 `{"error": "INVITATION_ALREADY_USED"}`

#### Scenario: Token inexistente
- **WHEN** token não encontrado em `invitations`
- **THEN** 404 `{"error": "INVITATION_NOT_FOUND"}`

#### Scenario: Usuário já é membro
- **WHEN** aceite de token válido mas usuário já é membro ativo da org
- **THEN** 409 `{"error": "ALREADY_A_MEMBER"}`

#### Scenario: Membro removido volta pelo convite
- **WHEN** aceite de token válido por um usuário que foi removido da org (vínculo com `deleted_at`)
- **THEN** 204; o vínculo removido é reativado — `deleted_at` limpo, papel do convite, `joined_at` da volta — sem criar outra linha (`uq_member` ignora `deleted_at`); token marcado `USED`

#### Scenario: Usuário não autenticado
- **WHEN** `POST /invitations/{token}/accept` sem JWT
- **THEN** 401; frontend redireciona para `/register?invite={token}`

---

### REQ-INV-03 — Remover membro
`ADMIN_ORG` pode remover qualquer membro da organização exceto o owner (identificado pelo `owner_id` em `organizations`). Remoção aplica soft delete em `organization_members` (seta `deleted_at`).

#### Scenario: Remoção bem-sucedida
- **WHEN** `DELETE /organizations/{id}/members/{userId}` com JWT `org=orgId, roles=[ADMIN_ORG]`
- **THEN** 204; `organization_members` tem `deleted_at` preenchido para o vínculo

#### Scenario: Tentativa de remover o owner
- **WHEN** `userId` no path é o `owner_id` da organização
- **THEN** 403 `{"error": "CANNOT_REMOVE_OWNER"}`

#### Scenario: Membro não encontrado
- **WHEN** `userId` não é membro ativo da organização
- **THEN** 404 `{"error": "MEMBER_NOT_FOUND"}`

---

### REQ-INV-04 — Frontend: página de aceite de convite
A rota `/invitations/:token/accept` verifica autenticação; se não autenticado, redireciona para `/register?invite={token}`. Se autenticado, exibe tela de confirmação com nome da organização e papel, e executa o aceite ao confirmar.

#### Scenario: Usuário autenticado aceita convite
- **WHEN** usuário acessa `/invitations/:token/accept` com sessão ativa
- **THEN** página exibe org e papel; ao confirmar, `POST /invitations/{token}/accept`; redirecionar para `/organizations/{orgId}` após sucesso

#### Scenario: Usuário não autenticado
- **WHEN** usuário acessa `/invitations/:token/accept` sem sessão
- **THEN** redirecionar para `/register?invite={token}`

---

### Requirement: An invitation is accepted only by its addressee
The invitation link is secret but is not a credential. Accepting SHALL require that the invitation's email matches the authenticated user's email, compared case-insensitively.

#### Scenario: Someone else holds the token
- **WHEN** an authenticated user whose email differs from the invitation's posts the accept endpoint
- **THEN** the system returns `403 INVITATION_NOT_FOR_THIS_USER` and creates no membership

#### Scenario: The addressee accepts
- **WHEN** the invited user accepts, even having signed up with the email in a different case
- **THEN** the system returns 204 and creates the membership with the role carried by the invitation

#### Scenario: The accepting user is unknown
- **WHEN** the authenticated subject has no matching user record
- **THEN** the system returns `403 INVITATION_NOT_FOR_THIS_USER`, revealing nothing about the invitation

---

### Requirement: Accepting an invitation enters the organization
Accepting is entering a new organization, so the web app SHALL reissue the access token for that organization and drop the cached queries before navigating. Without it the JWT still points at the previous organization (or none) and the destination screen answers 403.

#### Scenario: Invitation accepted
- **WHEN** the user accepts an invitation
- **THEN** the app switches the session to that organization, clears the query cache and navigates to it

#### Scenario: Accepting fails
- **WHEN** the accept request fails
- **THEN** the session is left untouched — no token switch and no cache clear

---

### Requirement: A pending invitation finds its addressee after sign-up
Between the invitation link and the accept screen sits the confirmation email, possibly opened in another browser, so neither the URL nor `localStorage` survives the trip. The system SHALL expose the pending, unexpired invitations addressed to the authenticated user's email, and the web app SHALL follow one when a user arrives without an organization.

#### Scenario: Invited user signs in without an organization
- **WHEN** an authenticated user with no organization lands on the root route
- **THEN** the app looks up the pending invitations and navigates to the most recent one's accept screen

#### Scenario: No invitation waiting
- **WHEN** the same user has no pending invitation
- **THEN** the app navigates to `/welcome` as before

#### Scenario: User already belongs to an organization
- **WHEN** a user with an organization lands on the root route
- **THEN** the app goes straight to `/classrooms` without looking up invitations

#### Scenario: The inviting organization is gone
- **WHEN** a pending invitation points at an organization that no longer exists
- **THEN** it is omitted from the list, since it has nowhere to lead

---

### Requirement: The invitation survives the login screen
A logged-out visitor opening an invitation SHALL be sent to login carrying the token, and be returned to the accept screen once signed in — whatever state the invitation is in, since the accept screen is what explains it. Registration is reached from the login screen, which passes the token along. The guard on public pages SHALL follow `?invite=` too, so it never overrides that return with a redirect to the root.

#### Scenario: Logged-out visitor opens the invitation link
- **WHEN** an unauthenticated user opens `/invitations/<token>/accept`
- **THEN** the app redirects to `/login?invite=<token>`

#### Scenario: Signing in with an invitation pending
- **WHEN** the user signs in from `/login?invite=<token>`
- **THEN** the app navigates to `/invitations/<token>/accept` instead of the root

#### Scenario: The invitee has no account yet
- **WHEN** the user follows "Criar conta" from `/login?invite=<token>`
- **THEN** the registration link carries `?invite=<token>` forward

#### Scenario: Signing in from a link that is no longer pending
- **WHEN** the user signs in from `/login?invite=<token>` and that invitation was cancelled, resent or expired
- **THEN** the app lands on `/invitations/<token>/accept`, not on `/welcome`

#### Scenario: Already signed in on a public page with an invitation
- **WHEN** an authenticated user opens `/login?invite=<token>` or `/register?invite=<token>`
- **THEN** the app redirects to `/invitations/<token>/accept`; without `?invite=` it redirects to the root

#### Scenario: Signing out on the accept screen
- **WHEN** an authenticated user on `/invitations/<token>/accept` clicks "Sair"
- **THEN** the app goes to `/login` without `?invite=`, so the next account to sign in lands in the app, not on that invitation; losing the session any other way still keeps the token

---

### Requirement: Only one invitation link is valid per email
Re-inviting an email SHALL cancel its pending, unexpired invitation in the same organization, in the same transaction, before issuing and mailing a new token. Resending is re-inviting.

#### Scenario: The admin resends an invitation
- **WHEN** `POST /organizations/{id}/invitations` targets an email that already has a pending invitation there, in any letter case
- **THEN** the previous invitation becomes `CANCELLED` and a new `PENDING` one is created with a different token

---

### Requirement: The admin sees every invitation of the organization
`GET /organizations/{id}/invitations` SHALL list the organization's invitations in any state, newest first, with email, role, effective status, inviter name, `createdAt` and `expiresAt`. The effective status reports a `PENDING` invitation past its expiry as `EXPIRED`; `EXPIRED` is never stored. The token SHALL NOT be returned.

#### Scenario: Invitations in every state
- **WHEN** the organization has pending, expired, used and cancelled invitations
- **THEN** 200 lists them newest first as `PENDING`, `EXPIRED`, `USED`, `CANCELLED`, without a `token` field

#### Scenario: Another organization's admin
- **WHEN** the JWT `org` claim differs from `{id}`, or the caller is not `ADMIN_ORG`
- **THEN** 403

---

### Requirement: The admin cancels a pending invitation
`DELETE /organizations/{id}/invitations/{invitationId}` SHALL cancel a pending, unexpired invitation. Accepting a cancelled invitation SHALL answer `410 INVITATION_CANCELLED`, and the accept screen SHALL say the admin cancelled it rather than that it expired.

#### Scenario: Pending invitation cancelled
- **WHEN** the admin deletes a pending invitation of their organization
- **THEN** 204 and the invitation is `CANCELLED`

#### Scenario: Invitation no longer pending
- **WHEN** the invitation is used, cancelled or expired
- **THEN** 409 `INVITATION_NOT_PENDING` and nothing changes

#### Scenario: Invitation of another organization
- **WHEN** the invitation id belongs to a different organization
- **THEN** 404 `INVITATION_NOT_FOUND`, revealing nothing about it

#### Scenario: The invitee opens a cancelled link
- **WHEN** an authenticated user accepts a cancelled invitation
- **THEN** 410 `INVITATION_CANCELLED`, no membership is created, and the page shows "Este convite foi cancelado pelo administrador"

---

### Requirement: The admin and the manager search the organization's members
`GET /organizations/{id}/members` SHALL return a page of the organization's active members, ordered by name, and SHALL accept `search`, `page` and `size`. `search` matches the member's name or email, case-insensitively, anywhere in the value; null or blank does not filter. The response carries the paginated envelope `{ content, totalElements, totalPages, number, size }`, with `size` defaulting to 20 and capped at 100. Removed memberships (soft delete) never appear.

#### Scenario: Members without a search term
- **WHEN** an `ADMIN_ORG` or `GESTOR` of the organization calls the endpoint with no `search`
- **THEN** 200 with every active member in `content`, ordered by name, and the totals describing the whole set

#### Scenario: The search matches name or email
- **WHEN** `search` is part of a member's name or email, in any letter case
- **THEN** 200 with only the matching members; a term matching nothing returns an empty `content` with `totalElements` 0

#### Scenario: Paging through the members
- **WHEN** `page` and `size` split the result
- **THEN** each response carries that slice in `content`, `totalElements` and `totalPages` describe the whole set, and `number` echoes the requested page

#### Scenario: Another organization's member listing
- **WHEN** the JWT `org` claim differs from `{id}`, or the caller is neither `ADMIN_ORG` nor `GESTOR`
- **THEN** 403
