## Why

A análise de arquitetura do frontend identificou 19 violações ativas das decisões documentadas em `DECISIONS.md` e nos requisitos RF-02 e RF-12. As mais críticas expõem o Access Token a ataques XSS (localStorage), permitem que alunos acessem rotas de professor (ProtectedRoute sem verificação de papel) e criam acoplamento proibido entre features. Esta change elimina a dívida técnica acumulada desde o início da implementação e torna o frontend compliant com as regras invioláveis do projeto antes de avançar para novos RFs.

## What Changes

- **BREAKING** `authStore`: Access Token migrado de `localStorage` para memória Zustand; `role`, `userId` e `organizationId` expostos como campos decodificados do JWT — elimina `localStorage.getItem('access_token')` e `localStorage.getItem('organization_id')` de toda a codebase
- `ProtectedRoute` passa a aceitar prop `roles?: string[]` e bloqueia acesso por papel
- Rota raiz `/` criada com redirect inteligente por contexto (tem org → `/classrooms`, sem org → `/organizations/new`)
- Decodificação manual de JWT (`atob/JSON.parse`) removida de 4 componentes de apresentação
- `SubmissionStatus` recebe valor `LATE` (RF-12)
- Interceptor Axios atualiza `authStore` após refresh silencioso do token
- Schemas Zod adicionados a todos os arquivos `features/*/api/` para validação em runtime das respostas
- `AppShell` com Sidebar contextual ao papel e Header criados em `components/layout/`
- Todas as rotas protegidas envolvidas pelo layout route do AppShell
- Shadcn `Dialog`, `Sheet` e `AlertDialog` instalados; overlays manuais (`div fixed inset-0`) substituídos
- `LoginPage`/`RegisterPage` separadas da lógica de mutation (FE-02)
- `useSessionInit` e `ConfirmEmailCallbackPage` migrados de `useEffect` para TanStack Query
- Rotas públicas `/register`, `/forgot-password`, `/reset-password` envolvidas em `PublicRoute`
- `queryFn` inline em `TaskListPage` extraído para `assessment/api/tasks.ts`
- `API_BASE_URL` manual em `SubjectDetailPage` removida; usa `baseURL` de `src/lib/axios.ts`
- Cores Tailwind absolutas substituídas por tokens semânticos Shadcn
- `window.confirm()` substituído por `ConfirmDialog` (`AlertDialog` Shadcn)
- `eslint.config.js` e `.prettierrc` criados

## Capabilities

### New Capabilities
- `app-layout`: AppShell com Sidebar de navegação e Header contextual ao papel do usuário
- `route-authorization`: ProtectedRoute com verificação de papel além de autenticação

### Modified Capabilities
- `authentication`: Token em memória (não localStorage); `role`/`organizationId`/`userId` decodificados do JWT e expostos pelo `authStore`
- `task-submission`: `SubmissionStatus` inclui `LATE` conforme RF-12

## Impact

- **Frontend apenas** (`apps/web/src/`)
- Arquivos afetados: `authStore.ts`, `axios.ts`, `routes.tsx`, `ProtectedRoute.tsx`, `useSessionInit.ts`, `ConfirmEmailCallbackPage.tsx`, `useLogin.ts`, `LoginForm.tsx`, `RegisterForm.tsx`, todos `features/*/api/*.ts`, `TaskListPage.tsx`, `SubjectDetailPage.tsx`, `SubjectListPage.tsx`, `ClassroomListPage.tsx`, `ClassroomDetailPage.tsx`, `assessment/types.ts`, `EvaluationDialog.tsx`, `SubmissionListDrawer.tsx`, `ContentFormDialog.tsx`
- Novas dependências: componentes Shadcn adicionais (`dialog`, `sheet`, `alert-dialog`)
- Sem alterações de schema de banco, sem mudança de contratos de API, sem impacto no backend

## Non-goals

- Implementar features `dashboard` e `communication` (RF-14 a RF-20)
- Adicionar testes unitários de componentes (pode ser feito em change separada)
- Alterar endpoints do backend
