## MODIFIED Requirements

### Requirement: REQ-ORG-01 — Criar organização
Usuário autenticado com e-mail confirmado (`status=ACTIVE`) pode criar uma organização informando `name` (obrigatório, único por usuário) e `description` (opcional). O sistema persiste em `organizations` e cria vínculo em `organization_members` com `role=ADMIN_ORG`. O `owner_id` da organização é o userId do criador e é usado para impedir sua remoção (REQ-INV-03).

#### Scenario: Criação bem-sucedida
- **WHEN** `POST /organizations` com JWT válido (`sub=userId`), body `{name, description?}`
- **THEN** 201 com `{id, name, description, createdAt}`; `organizations` contém novo registro com `owner_id=userId`; `organization_members` contém `(userId, orgId, ADMIN_ORG)`
