# fix-fe-arch-breaks — Delta

## Added
- Capability `app-layout`: `AppShell` (Header + Sidebar contextual ao papel) envolve todas as rotas autenticadas; rotas públicas (`/login`, `/register`, `/forgot-password`, `/reset-password`, `/confirm-email`) não o renderizam.
- Capability `route-authorization`: `ProtectedRoute` aceita a prop `roles?: string[]` e bloqueia acesso quando o papel do usuário não está na lista permitida.
- `authentication`: interceptor Axios sincroniza o `authStore` com o novo Access Token após o refresh silencioso disparado no tratamento de 401.
- `task-submission`: frontend reconhece o status `LATE` — `SubmissionStatus` passa a incluir `'LATE'` no union type.
- Rota raiz `/` com redirect por contexto: com organização → `/classrooms`; sem organização → `/organizations/new`.
- Schemas Zod em todos os arquivos `features/*/api/`, validando as respostas em runtime.

## Changed
- **BREAKING** `authentication` (REQ-AUTH-07): o Access Token sai de `localStorage` e passa a viver apenas em memória no `authStore` (Zustand), que agora expõe `role`, `userId` e `organizationId` decodificados do JWT no `setToken`.

## Removed
- `localStorage.getItem('access_token')` e `localStorage.getItem('organization_id')` de toda a codebase — o token em `localStorage` era o vetor de XSS que motivou a change.
- Decodificação manual de JWT (`atob`/`JSON.parse`) em 4 componentes de apresentação.
