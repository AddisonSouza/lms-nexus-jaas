## MODIFIED Requirements

### Requirement: REQ-AUTH-07 — Frontend: persistência de sessão
O Access Token MUST ser mantido apenas em memória no `authStore` (Zustand) — NUNCA em `localStorage` ou `sessionStorage`. Ao recarregar a página, o sistema MUST tentar renovar a sessão via `POST /auth/refresh` (usando o `httpOnly cookie`) antes de redirecionar para login. O `authStore` MUST expor os campos `role`, `userId` e `organizationId` decodificados do payload do JWT no momento do `setToken`.

#### Scenario: Reload da página com refresh token válido
- **WHEN** usuário recarrega a página com `httpOnly cookie` de Refresh Token válido
- **THEN** sistema chama `POST /auth/refresh`, obtém novo Access Token, popula `authStore` com `accessToken`, `role`, `userId` e `organizationId` — e exibe a app sem redirecionar para login

#### Scenario: Reload da página com refresh token expirado ou ausente
- **WHEN** usuário recarrega a página sem `httpOnly cookie` ou com cookie expirado
- **THEN** sistema detecta 401 na chamada de refresh, limpa o `authStore` e redireciona para `/login`

#### Scenario: Access Token não persiste em localStorage
- **WHEN** usuário faz login com sucesso
- **THEN** `localStorage.getItem('access_token')` retorna `null` — token somente em memória

#### Scenario: authStore expõe role decodificado
- **WHEN** `setToken(accessToken)` é chamado com token que contém `roles: ['PROFESSOR']`
- **THEN** `useAuthStore.getState().role` retorna `'PROFESSOR'` sem que nenhum componente precise decodificar o JWT

## ADDED Requirements

### Requirement: Interceptor Axios sincroniza authStore após refresh silencioso
O sistema SHALL atualizar o `authStore` com o novo Access Token após o refresh silencioso realizado pelo interceptor de resposta do Axios (tratamento de 401).

#### Scenario: Refresh silencioso bem-sucedido
- **WHEN** interceptor Axios detecta 401, chama `POST /auth/refresh` com sucesso e obtém novo `accessToken`
- **THEN** `useAuthStore.getState().setToken(newAccessToken)` é chamado — store e header Axios ficam sincronizados
