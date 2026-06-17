## Context

Encontrado durante a verificação manual do RF-17: `AuthenticateService.execute()` (login) sempre chama `jwtTokenService.generateAccessToken(userId)` (sem org/role); `RefreshTokenService.execute()` só resolve `org`/`role` quando o cliente envia `organizationId` explicitamente no body — único lugar que faz isso hoje é `useCreateOrganization` no frontend, imediatamente após criar uma organização. Não existe, no frontend, nenhuma tela ou fluxo de "selecionar organização" para um membro que faz login normalmente.

Já existe toda a infraestrutura necessária para o fix: `TokenGeneratorPort.generateAccessToken(userId, orgId, role)` (usado pelo `RefreshTokenService` no caminho com `organizationId`) e `OrganizationMemberLookupPort.findRoleByUserAndOrg(userId, orgId)`, implementado em `organization/infrastructure/persistence/OrganizationMemberRepositoryImpl` (adapter cross-module, mesmo padrão usado em outros Ports do projeto).

## Goals / Non-Goals

**Goals:**
- `POST /auth/login` e `POST /auth/refresh` (sem `organizationId`) embutem `org`/`roles` automaticamente quando o usuário pertence a exatamente uma organização ativa.
- Nenhuma mudança de contrato HTTP (mesmo formato de request/response) e nenhuma mudança no frontend — `authStore.setToken` já decodifica `org`/`groups` quando presentes.

**Non-Goals:**
- Seleção explícita de organização para usuários com múltiplos vínculos (telas/endpoint de "minhas organizações") — change futura.
- Mudar o comportamento de `RefreshTokenService` quando `organizationId` já é informado explicitamente (já funciona corretamente).
- Revisar/alterar TTLs, rotation de refresh token ou qualquer outra regra de segurança já existente (SEC-02/03).

## Decisions

**1. Novo método `List<OrgMembership> findOrganizationsByUser(String userId)` em `OrganizationMemberLookupPort` (módulo `identity`), em vez de um método "findSoleOrganization".**
Retornar a lista completa (não só "a única, se houver") deixa a decisão de "exatamente uma" no use case (`AuthenticateService`/`RefreshTokenService`), que é onde a regra de negócio pertence — e deixa o método reutilizável por uma futura tela de seleção de organização (Non-Goal aqui, mas o Port já fica pronto). Alternativa considerada: método que já retorna `Optional<OrgMembership>` só quando há exatamente uma — rejeitada por escond er a contagem real (0 vs. 2+ organizações) do chamador, que pode ser útil para decisões futuras (ex.: mostrar "crie uma organização" vs. "selecione uma organização").

`OrgMembership` é um novo VO simples (`organizationId`, `role`) em `identity/domain/model/`, espelhando o uso já existente de `String role` solto em `findRoleByUserAndOrg` — mantido como `record` para os dois campos virem juntos sem precisar de duas queries.

**2. Implementação do novo método no adapter já existente `OrganizationMemberRepositoryImpl` (módulo `organization`), mesmo padrão de `findRoleByUserAndOrg`.**
JPQL simples: `SELECT m.organizationId, m.role FROM OrganizationMemberJpaEntity m WHERE m.userId = :userId AND m.deletedAt IS NULL`. Mesma regra de soft delete já aplicada nos outros métodos do mesmo adapter.

**3. `AuthenticateService` e `RefreshTokenService` (caminho sem `organizationId`) chamam `findOrganizationsByUser(userId)`; se a lista tiver tamanho 1, geram o token com `org`/`role` dessa organização; caso contrário, mantêm o comportamento atual (token sem essas claims).**
Alternativa considerada: resolver isso no `TokenGeneratorPort`/`JwtTokenService` (módulo de infraestrutura de segurança) — rejeitada porque a consulta a `organization_members` é uma regra de aplicação (cross-module), não uma responsabilidade de geração de token; manter a decisão nos Use Cases preserva a regra MOD-01 (módulos comunicam via Ports) sem misturar responsabilidades.

**4. Sem mudança de contrato HTTP nem de DTO de resposta.**
`AuthResult(accessToken, refreshToken)` permanece igual; só o conteúdo do JWT muda. O frontend não precisa de nenhuma alteração: `authStore.setToken` já decodifica `org`/`groups` do payload quando presentes (e já trata a ausência como `null`/`[]`, comportamento idêntico ao atual quando há 0 ou 2+ organizações).

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/identity/
  domain/
    model/OrgMembership.java (novo — record: organizationId, role)
    port/out/OrganizationMemberLookupPort.java (ganha findOrganizationsByUser)
  application/
    usecase/AuthenticateService.java (passa a resolver org/role antes de gerar o token)
    usecase/RefreshTokenService.java (mesma resolução no caminho sem organizationId)

apps/api/src/main/java/br/edu/lms/module/organization/
  infrastructure/persistence/OrganizationMemberRepositoryImpl.java (implementa findOrganizationsByUser)
```

Nenhuma migration Flyway é necessária — nenhuma mudança de schema.

## Risks / Trade-offs

- [Risco] Usuário com múltiplas organizações continua sem conseguir acessar a segunda/terceira organização via login normal (Non-Goal) — mitigação: comportamento idêntico ao atual, não é uma regressão; abre caminho (Port já pronto) para a tela de seleção numa change futura.
- [Trade-off] A resolução acontece a cada login/refresh (uma query extra) — aceitável dado o volume do MVP; mesma característica de qualquer lookup de membership já feito em outros endpoints do projeto.
