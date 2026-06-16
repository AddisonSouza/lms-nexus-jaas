## 1. [FE] authStore — Token em Memória e Campos JWT Decodificados

- [ ] 1.1 [FE] Remover `localStorage.setItem/getItem('access_token')` e `localStorage.setItem/getItem('organization_id')` de `authStore.ts`; manter `accessToken` apenas em estado Zustand
- [ ] 1.2 [FE] Em `setToken(token)`, decodificar payload JWT e popular `role: string | null`, `userId: string | null`, `organizationId: string | null` no estado do store
- [ ] 1.3 [FE] Atualizar interceptor de request do `axios.ts` para ler `useAuthStore.getState().accessToken` em vez de `localStorage.getItem('access_token')`
- [ ] 1.4 [FE] Após refresh silencioso no interceptor de resposta do `axios.ts` (tratamento de 401), chamar `useAuthStore.getState().setToken(data.accessToken)` para sincronizar store e Axios

## 2. [FE] Corrigir Imports Cruzados entre Features

- [ ] 2.1 [FE] Remover import de `refreshTokens` de `features/auth/api/auth-api` em `features/organization/hooks/useCreateOrganization.ts`; responsabilidade de refresh já está no interceptor Axios
- [ ] 2.2 [FE] Em `features/assessment/components/TaskListPage.tsx`, criar hook local `useSubjectList` em `features/assessment/hooks/` que faz própria query `GET /subjects` em vez de importar de `features/curriculum`
- [ ] 2.3 [FE] Remover import de `@features/curriculum/api/query-keys` e `@features/curriculum/api/subject-api` de `features/assessment/components/TaskListPage.tsx`

## 3. [FE] authStore — Remover Decodificação Manual de JWT dos Componentes

- [ ] 3.1 [FE] Substituir bloco `const token = useAuthStore(...); const role = token ? JSON.parse(atob(...)) : 'ALUNO'` em `ClassroomListPage.tsx` por `const role = useAuthStore(s => s.role)`
- [ ] 3.2 [FE] Substituir mesmo padrão em `ClassroomDetailPage.tsx`
- [ ] 3.3 [FE] Substituir mesmo padrão em `SubjectListPage.tsx`
- [ ] 3.4 [FE] Substituir mesmo padrão em `SubjectDetailPage.tsx`

## 4. [FE] Roteamento — Rota Raiz e Rotas Públicas

- [ ] 4.1 [FE] Criar rota `'/'` em `routes.tsx` que redireciona: se `organizationId` presente no authStore → `/classrooms`; caso contrário → `/organizations/new`
- [ ] 4.2 [FE] Atualizar `useLogin.ts` para navegar para `'/'` (já faz isso — validar que a rota agora existe)
- [ ] 4.3 [FE] Envolver rotas `/register`, `/forgot-password` e `/reset-password` em `<PublicRoute>` em `routes.tsx`
- [ ] 4.4 [FE] Criar `RegisterPage.tsx` em `features/auth/components/` que chama `useRegister()` e passa `onSubmit`/`isPending`/`error` como props para `RegisterForm`; atualizar `routes.tsx` para usar `<RegisterPage />`
- [ ] 4.5 [FE] Refatorar `LoginPage.tsx` para chamar `useLogin()` e passar callbacks como props para `LoginForm`; `LoginForm` passa a ser puro (recebe props, sem hooks de mutation)

## 5. [FE] ProtectedRoute com Verificação de Papel

- [ ] 5.1 [FE] Adicionar prop `roles?: string[]` a `ProtectedRoute.tsx`; se `roles` informado, verificar `authStore.role` está na lista — caso contrário redirecionar para `'/'`
- [ ] 5.2 [FE] Mapear rotas em `routes.tsx` com suas permissões: `/assessment/tasks` → `['PROFESSOR']`; `/assessment/student-tasks` → `['ALUNO']`

## 6. [FE] AppShell — Layout e Navegação

- [ ] 6.1 [FE] Instalar componentes Shadcn necessários: `npx shadcn@latest add dialog sheet alert-dialog`; verificar diff de `tailwind.config.ts` e `globals.css`
- [ ] 6.2 [FE] Criar `src/components/layout/Header.tsx` com nome/email do usuário e botão de logout (chama `POST /auth/logout`, limpa authStore, navega para `/login`)
- [ ] 6.3 [FE] Criar `src/components/layout/Sidebar.tsx` com links contextuais ao papel (PROFESSOR: Turmas, Disciplinas, Tarefas; ALUNO: Turmas, Minhas Tarefas; ADMIN_ORG/GESTOR: Turmas, Disciplinas, Tarefas, Membros)
- [ ] 6.4 [FE] Criar `src/components/layout/AppShell.tsx` que compõe `Header` + `Sidebar` + `<Outlet />`
- [ ] 6.5 [FE] Envolver todas as rotas protegidas em `routes.tsx` em um layout route `<Route element={<ProtectedRoute><AppShell /></ProtectedRoute>}>` sem `path`

## 7. [FE] Shadcn — Substituir Overlays Manuais

- [ ] 7.1 [FE] Substituir overlay manual `fixed inset-0` em `EvaluationDialog.tsx` pelo componente `<Dialog>` do Shadcn
- [ ] 7.2 [FE] Substituir overlay manual em `SubmissionListDrawer.tsx` pelo componente `<Sheet>` do Shadcn
- [ ] 7.3 [FE] Substituir overlay manual em `ContentFormDialog.tsx` (`features/curriculum`) pelo componente `<Dialog>` do Shadcn
- [ ] 7.4 [FE] Substituir overlay manual em `SubjectDetailPage.tsx` (modal de conteúdo) pelo componente `<Dialog>` do Shadcn
- [ ] 7.5 [FE] Criar `src/components/shared/ConfirmDialog.tsx` usando `<AlertDialog>` do Shadcn
- [ ] 7.6 [FE] Substituir `window.confirm()` por `<ConfirmDialog>` em `SubjectListPage.tsx` (exclusão de disciplina), `SubjectDetailPage.tsx` (exclusão de tópico e conteúdo) e `ClassroomDetailPage.tsx`

## 8. [FE] useSessionInit — Migrar de useEffect para TanStack Query

- [ ] 8.1 [FE] Em `useSessionInit.ts`, converter `useEffect` + `refreshTokens()` para `useQuery({ queryKey: ['session'], queryFn: refreshTokens, enabled: !isAuthenticated })`; no `onSuccess`, chamar `setToken`; no `onError`, chamar `clearToken` e navegar para `/login`
- [ ] 8.2 [FE] Em `ConfirmEmailCallbackPage.tsx`, remover `useEffect` que dispara mutation; substituir por chamada direta na montagem usando `useMutation` com `onMount` ou estruturar como query com `enabled: !!token`

## 9. [FE] Zod — Validação de Respostas de API

- [ ] 9.1 [FE] Criar schemas Zod de resposta em `features/auth/api/auth-api.ts`: `loginResponseSchema`, `refreshResponseSchema` e chamar `.parse()` antes de retornar
- [ ] 9.2 [FE] Criar schemas Zod em `features/assessment/api/tasks.ts`: `taskSchema`, `taskListSchema`; aplicar `.parse()` em todas as funções
- [ ] 9.3 [FE] Criar schemas Zod em `features/assessment/api/submissions.ts`: `submissionSchema`, `submissionListSchema`; aplicar `.parse()`
- [ ] 9.4 [FE] Criar schemas Zod em `features/curriculum/api/subject-api.ts`: `subjectSchema`, `subjectListSchema`, `topicSchema`, `contentSchema`; aplicar `.parse()`
- [ ] 9.5 [FE] Criar schemas Zod em `features/classroom/api/` (se existir) ou no arquivo de API correspondente; aplicar `.parse()`
- [ ] 9.6 [FE] Criar schemas Zod em `features/organization/api/` e `features/invitation/api/`; aplicar `.parse()`

## 10. [FE] Fixes de Tipo e Contratos

- [ ] 10.1 [FE] Adicionar `'LATE'` ao union type `SubmissionStatus` em `features/assessment/types.ts`
- [ ] 10.2 [FE] Em `SubmissionListDrawer.tsx`, atualizar condição do botão "Avaliar" para `sub.status === 'SUBMITTED' || sub.status === 'LATE'` (submissões atrasadas também podem ser avaliadas)
- [ ] 10.3 [FE] Extrair `queryFn` inline de `TaskListPage.tsx` para função `listTasks(): Promise<Task[]>` em `features/assessment/api/tasks.ts`
- [ ] 10.4 [FE] Remover `API_BASE_URL` construída manualmente em `SubjectDetailPage.tsx`; usar instância `api` de `src/lib/axios.ts` em todas as chamadas do arquivo

## 11. [FE] Design System — Tokens Semânticos e Tooling

- [ ] 11.1 [FE] Substituir cores Tailwind absolutas em `RegisterForm.tsx` pelos tokens semânticos Shadcn (`bg-primary`, `text-destructive`, `bg-background`, `text-foreground`, `border-input`)
- [ ] 11.2 [FE] Substituir cores em `ClassroomListPage.tsx` e `ClassroomDetailPage.tsx` (`bg-green-100 text-green-700` → badge Shadcn; `bg-gray-100 text-gray-600` → `bg-muted text-muted-foreground`)
- [ ] 11.3 [FE] Substituir cores em `EvaluationDialog.tsx` (`text-red-500` → `text-destructive`; `text-blue-600` → `text-primary`) e `ConfirmEmailCallbackPage.tsx` (`text-green-500` → token customizado)
- [ ] 11.4 [FE] Criar `eslint.config.js` (flat config) com `@typescript-eslint/recommended`, plugin `react-hooks` e regras `react-hooks/exhaustive-deps: error` e `@typescript-eslint/no-explicit-any: error`
- [ ] 11.5 [FE] Instalar `prettier` e criar `.prettierrc` com configuração padrão; adicionar script `"format": "prettier --write src/"` ao `package.json` de `apps/web`
