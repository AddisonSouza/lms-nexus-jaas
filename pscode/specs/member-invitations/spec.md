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
