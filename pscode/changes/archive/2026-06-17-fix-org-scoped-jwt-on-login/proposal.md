## Why

Encontrado durante a verificação manual do RF-17 (módulo `reporting`): `POST /auth/login` e `POST /auth/refresh` (sem `organizationId`) emitem um access token sem as claims `org` e `groups`. Hoje, a única rota que produz um token com essas claims é `useCreateOrganization` no frontend, chamando `refreshTokens(orgId)` imediatamente após criar uma organização — ou seja, apenas quem **cria** a organização, no momento da criação, recebe um token com papel. Qualquer membro existente (convidado como `GESTOR`, `PROFESSOR`, `ALUNO`, ou o próprio `ADMIN_ORG` numa sessão nova) faz login normalmente mas nunca obtém esse token escopado, ficando sem acesso efetivo a endpoints protegidos por `@RolesAllowed` e a dashboards (RF-17/18/19/20).

## What Changes

- `POST /auth/login` e `POST /auth/refresh` (caminho sem `organizationId` explícito) passam a resolver automaticamente a organização do usuário quando ele pertence a **exatamente uma**, embutindo `org` e `groups` no token emitido — sem exigir nenhuma ação extra do usuário.
- Quando o usuário pertence a zero ou a mais de uma organização, o comportamento atual é mantido (token sem `org`/`groups`) — seleção explícita entre múltiplas organizações é tratada em change futura.
- Novo método de consulta no Port já existente (`OrganizationMemberLookupPort`) para listar as organizações (`organizationId` + `role`) de um usuário.
- **Fora de escopo nesta change:** tela/endpoint de seleção de organização para usuários com múltiplos vínculos; `RootRedirect` continuará mandando esses casos (raros hoje) para `/organizations/new`.

## Capabilities

### New Capabilities

(nenhuma — esta change corrige o comportamento de uma capability já existente, não introduz uma nova)

### Modified Capabilities
- `authentication`: login e refresh (sem organização explícita) passam a resolver e embutir `org`/`groups` no token quando o usuário pertence a exatamente uma organização.

## Impact

- **Backend:** `AuthenticateService` (login) e `RefreshTokenService` (caminho sem `organizationId`) passam a consultar `OrganizationMemberLookupPort` antes de gerar o token; novo método no Port (`identity/domain/port/out/`) implementado em `OrganizationMemberRepositoryImpl` (módulo `organization`, mesmo padrão cross-module já usado por `findRoleByUserAndOrg`). Sem migration — não há mudança de schema.
- **Frontend:** nenhuma mudança necessária — `authStore.setToken` já decodifica `org`/`groups` do token quando presentes; `RootRedirect` e `ProtectedRoute` já leem esses campos corretamente.
- Sem impacto em contratos REST existentes — o formato da resposta de `/auth/login` e `/auth/refresh` não muda, apenas o conteúdo do JWT retornado.
