## Context

O frontend `apps/web` acumula 19 violações das decisões de arquitetura documentadas em `DECISIONS.md`. As violações foram identificadas em análise estática contra as regras FE-01 a FE-11, SEC-02, RF-02, RF-12, ADR-006 e as convenções de design system do projeto. O estado atual:

- `accessToken` salvo em `localStorage` → vulnerável a XSS
- `authStore` não decodifica o JWT → 4 componentes decodificam `atob/JSON.parse` manualmente
- 7 features importam diretamente de outras features (FE-11)
- `ProtectedRoute` só verifica autenticação, não papel → aluno acessa rota de professor
- Nenhum componente Shadcn instalado além de `@radix-ui/react-label` e `@radix-ui/react-slot` → overlays manuais com `fixed inset-0`
- Sem layout estrutural (`AppShell`, `Sidebar`) → sem navegação entre seções
- `SubmissionStatus` não inclui `LATE` → tipo diverge do contrato da API

## Goals / Non-Goals

**Goals:**
- Access Token em memória Zustand, nunca em disco
- `authStore` expõe `role`, `userId`, `organizationId` prontos — zero decodificação manual nos componentes
- Zero imports cruzados entre features
- `ProtectedRoute` bloqueia por papel além de autenticação
- Rota `/` funcional com redirect inteligente
- AppShell com Sidebar contextual ao papel envolvendo todas as rotas protegidas
- Shadcn `Dialog`, `Sheet`, `AlertDialog` substituindo todos os overlays manuais
- Zod `.parse()` em todos os arquivos `features/*/api/`
- Separação container/apresentação em Login e Register
- Rotas públicas com `PublicRoute`
- `SubmissionStatus` inclui `LATE`
- ESLint configurado + Prettier instalado

**Non-Goals:**
- Implementar features `dashboard` e `communication` (RF-14 a RF-20)
- Testes unitários de componentes (change separada)
- Mudança de contratos de API ou schema de banco
- Migração para React Server Components ou outro paradigma

## Decisions

### D1 — Access Token somente em memória Zustand

**Escolha:** Remover todos os `localStorage.setItem/getItem('access_token')` de `authStore.ts` e `axios.ts`. O token vive apenas no estado Zustand (puro, sem middleware `persist`).

**Motivo:** `localStorage` é acessível por qualquer script na página — ataque XSS pode exfiltrar o token. Zustand em memória limpa ao fechar a aba.

**Consequência:** Ao recarregar a página, `authStore.accessToken` é `null`. O `useSessionInit` chama `POST /auth/refresh` via `useQuery` (enabled: `!isAuthenticated`) para restaurar a sessão via `httpOnly cookie`. Este fluxo já existe — só precisa ser migrado de `useEffect` para `useQuery`.

**Alternativa descartada:** `sessionStorage` — ainda acessível por XSS dentro da mesma aba.

---

### D2 — `authStore` decodifica JWT e expõe campos prontos

**Escolha:** Em `setToken(token)`, decodificar o payload do JWT (`JSON.parse(atob(token.split('.')[1]))`) e armazenar `role: string | null`, `userId: string | null`, `organizationId: string | null` como campos do store.

**Motivo:** Elimina os 4 padrões `const role = token ? JSON.parse(...) : 'ALUNO'` duplicados em componentes. Centraliza a lógica de extração de claims.

**Alternativa descartada:** Biblioteca `jwt-decode` — overhead desnecessário para uma decodificação sem verificação de assinatura (que é responsabilidade do backend).

---

### D3 — Cross-feature imports: features leem `authStore` diretamente (não importam de `features/auth`)

**Escolha:** O `authStore` é um store Zustand em `src/features/auth/store/authStore.ts`. Features podem importar o store — isso não viola FE-11 pois stores são infraestrutura compartilhada, não "feature X importando de feature Y".

**Esclarecimento de FE-11:** A regra proíbe que `features/curriculum/` importe de `features/auth/api/` ou `features/auth/components/`. Importar o `authStore` (um store Zustand de estado compartilhado) é equivalente a importar de `src/store/` — aceitável desde que se limite ao store, não a lógica de API.

**Ação concreta:** O problema de cross-feature em `features/organization/hooks/useCreateOrganization.ts` (que importa `refreshTokens` de `features/auth/api`) DEVE ser corrigido movendo a responsabilidade de refresh para dentro do interceptor Axios — não chamando `refreshTokens` diretamente de outro módulo.

Para `features/assessment` importando de `features/curriculum`: criar hook local `useSubjectList` em `assessment/` que faz própria query `GET /subjects`.

---

### D4 — AppShell via React Router Layout Route

**Escolha:** Criar uma rota pai sem `path` (`<Route element={<AppShell />}>`) que envolve todas as rotas protegidas no `routes.tsx`.

```tsx
<Route element={<ProtectedRoute><AppShell /></ProtectedRoute>}>
  <Route path="/classrooms" element={<ClassroomListPage />} />
  <Route path="/curriculum" element={<SubjectListPage />} />
  ...
</Route>
```

**AppShell:** `Header` com nome do usuário e logout + `Sidebar` com links contextuais ao papel via `useAuthStore(s => s.role)`.

**Alternativa descartada:** Adicionar AppShell manualmente em cada página — duplicação e inconsistência.

---

### D5 — Shadcn instalado progressivamente

**Escolha:** Instalar apenas os componentes necessários para esta change: `dialog`, `sheet`, `alert-dialog`. O comando `npx shadcn@latest add dialog sheet alert-dialog` cria os arquivos em `src/components/ui/`.

**Motivo:** Instalar todos os ~50 componentes gera arquivos não utilizados. Cada change futura adiciona o que precisa.

---

### D6 — Zod para todos os arquivos de API (sem parse em endpoints de listagem paginada ainda)

**Escolha:** Adicionar schemas Zod e `.parse()` em todas as funções `features/*/api/*.ts`. Para arrays, usar `z.array(schema).parse(res.data)`.

**Ponto de atenção:** Se a API retornar campos extras não mapeados no schema, `.parse()` os remove por padrão. Usar `.strict()` apenas onde explicitamente necessário. Usar `.passthrough()` para endpoints onde campos extras devem ser preservados.

---

## Risks / Trade-offs

| Risco | Mitigação |
|---|---|
| Ao trocar `localStorage` por memória, sessão perde-se no reload até o refresh completar → flash de tela de login | Mostrar loading spinner no `useSessionInit` enquanto `isLoading` do `useQuery` é `true` — comportamento já existe parcialmente |
| Zod `.parse()` pode falhar se API retornar formato inesperado em produção | Usar `.safeParse()` com log de erro no console em dev; `.parse()` em prod (erro explícito é preferível a dado silenciosamente errado) |
| Shadcn `add` pode sobrescrever arquivos de configuração existentes (`tailwind.config.ts`, `globals.css`) | Revisar diff após `npx shadcn@latest add` antes de commitar |
| `ProtectedRoute` com `roles` pode bloquear rotas que hoje não exigem papel específico | Mapear cuidadosamente — rotas sem `roles` continuam permitindo qualquer autenticado |

## Migration Plan

1. **Sem breaking change de API** — toda mudança é frontend-only
2. **Ordem de implementação recomendada** (por dependência):
   1. Refatorar `authStore` + `axios.ts` (base de tudo)
   2. Instalar Shadcn + criar `ConfirmDialog`
   3. Criar `AppShell` + layout route
   4. Corrigir imports cruzados (dependem do authStore refatorado)
   5. Corrigir `ProtectedRoute` + rotas
   6. Adicionar Zod nas APIs
   7. Fixes menores (tipos, ESLint, cores)
3. **Rollback:** Revert do branch — sem schema migration, sem mudança de contrato

## Open Questions

- Nenhuma — todas as decisões foram resolvidas na fase de grill.
