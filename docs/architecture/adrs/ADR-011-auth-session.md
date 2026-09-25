# ADR-011 — Sessão com JWT em Memória e Refresh Token em Cookie

**Status:** Aceito  
**Data:** Setembro 2026 (registra decisão implementada entre RF-01 e RF-05)

## Contexto
A SPA (ADR-009) precisa autenticar contra a API sem servidor de sessão. Um usuário pode pertencer a várias Organizações com papéis diferentes, e o papel pode mudar enquanto ele está logado. Guardar tokens no `localStorage` os expõe a qualquer XSS.

## Decisão
- **Emissão própria via SmallRye JWT, RS256** — sem provedor OIDC externo. Claims: `sub`, `org`, `groups` (papel na org ativa), `name`, `email`.
- **Access token de 15 min, só em memória** (Zustand `authStore`, sem `persist`). Enviado como `Authorization: Bearer` pelo interceptor do Axios.
- **Refresh token opaco de 7 dias em cookie** `httpOnly`, `Secure`, `SameSite=Strict`, `Path=/auth`, guardado no Redis (ADR-002) e **rotacionado** a cada `/auth/refresh`.
- **Troca de organização** (`/auth/switch-organization`) emite um novo par de tokens com o `org` e o papel correspondentes.
- **Invalidação por mudança de vínculo:** o evento `OrganizationMembershipChangedEvent` grava `identity:stale-since:{userId}`; o `StaleSessionFilter` recusa com `401 SESSION_STALE` todo access token emitido antes da marca. O front trata o 401 renovando o token em silêncio, e a renovação relê o papel do banco.

## Justificativa
- JavaScript não lê o refresh token → um XSS não consegue sequestrar a sessão de longa duração
- `SameSite=Strict` + mesma origem (nginx serve SPA e `/api`, ver ADR-012) eliminam CSRF e CORS em produção
- Um IdP externo (Keycloak etc.) seria mais um serviço para operar sem ganho para o escopo
- Access token curto + marca de obsolescência dá revogação efetiva sem consultar o banco a cada requisição

## Consequências
- Recarregar a página perde o access token; o `AuthBootstrap` chama `/auth/refresh` na subida para restaurá-lo
- Toda requisição autenticada faz um `GET` no Redis (marca de obsolescência) — custo aceitável
- Front e API precisam estar na mesma origem em produção para o cookie `Strict` funcionar
- Chaves RSA fora do repositório em produção, montadas via `JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH`
