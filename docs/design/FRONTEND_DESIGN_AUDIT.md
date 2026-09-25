# Auditoria de Design do Frontend — LMS Nexus

> Documento de análise, gerado a partir da leitura do código-fonte (`apps/web/src`), das telas de referência (`*.png` na raiz do projeto) e das decisões arquiteturais existentes (`docs/architecture/adrs`). **Não implementa nada** — é insumo para um pass de redesign com linguagem visual estilo Claude/Anthropic.

---

## 1. Resumo executivo

O frontend do LMS Nexus (`apps/web`) é uma SPA React 18 + TypeScript bem estruturada em termos de **arquitetura de código** (feature-based, TanStack Query, react-hook-form + zod, Zustand, shadcn/ui sobre Radix/@base-ui), mas **visualmente está no estado "shadcn default" mais cru possível** — na prática, cru até para esse padrão, porque metade das telas nem usa os poucos componentes shadcn existentes.

O que os screenshots de referência mostram, sem exceção:

- **Zero cor.** Toda a paleta em `index.css` é `oklch(x 0 0)` — literalmente saturação zero em todos os tokens (`--primary`, `--accent`, `--ring`, `--chart-*` etc.). Isso não é "neutro com um acento quente": é preto/branco/cinza puro. Botões primários são cinza-escuro-quase-preto, não existe nenhuma cor de marca. O único lugar com cor de verdade são badges de status (`success`/`warning`/`destructive`), que também têm chroma baixo.
- **Hierarquia tipográfica rasa.** Praticamente todo título de página é `text-2xl font-semibold` (ou `font-bold`), todo corpo é `text-sm`, todo dado secundário é `text-xs text-muted-foreground`. Não há escala (display/heading/body/caption) nem peso variando por contexto — dá para contar nos dedos as combinações usadas em todas as ~50 telas/componentes.
- **Densidade e whitespace inconsistentes por acidente, não por sistema.** Cards têm `p-4`, dialogs `p-4`, containers de página `p-6`, mas isso não vem de uma escala de espaçamento — é o valor Tailwind "que pareceu ok" em cada arquivo. Não há tokens de espaçamento no `tailwind.config.js` (só `colors` e `borderRadius` são estendidos).
- **A maior parte das telas do produto (formulários, listas, cards de conteúdo) usa `<input>`, `<button>`, `<select>`, `<table>` HTML crus com classes Tailwind ad-hoc**, e não os componentes de `components/ui/`. Só diálogos, sheets, popover e confirmação usam os primitivos shadcn. Isso quer dizer que ~90% da superfície visual (todo formulário de auth, toda listagem em tabela, todo card de dashboard) está fora do design system nascente e cada arquivo reinventa `rounded border px-3 py-2 text-sm` na mão.
- **Estados vazios/loading são um parágrafo de texto cinza.** Não existem skeletons, ilustrações, ou qualquer tratamento — é sempre `<p className="text-sm text-muted-foreground">Carregando...</p>` ou "Nenhum X encontrado.".
- **`components/ui/` tem só 5 primitivos** (`button`, `dialog`, `alert-dialog`, `sheet`, `popover`) — faltam praticamente todos os blocos estruturais de uma UI de produto: `card`, `table`, `input`, `textarea`, `select`, `label`, `badge`, `tabs`, `avatar`, `tooltip`, `skeleton`, `separator`, `dropdown-menu`, `form` (wrapper react-hook-form), `toast/sonner`.
- Existe até **uma implementação de drawer própria e paralela ao `Sheet` já existente** (`GradeFeedbackDrawer.tsx` usa `fixed inset-0 bg-black/40` + `fixed inset-y-0 right-0` feitos à mão, ao invés do componente `Sheet` usado em `SubmissionListDrawer.tsx`) — sintoma de que não há uma disciplina de "sempre usar o primitivo compartilhado".

Nos screenshots (`rf20-student-dashboard.png`, `rf18-gestor-dashboard.png`, `rf10-subject-detail.png`, `verify-rf11-tasklist.png`, etc.) o resultado visual confirma a leitura do código: telas em preto e branco, títulos em negrito puro, cards brancos com borda cinza fina de 1px sem sombra, listas separadas por `border-b` fino ao invés de cards, nenhum ícone decorativo além dos utilitários (lucide-react pequenos e monocromáticos), sem avatares, sem badges de papel consistentes (aparecem só em `ClassroomMembersPanel`/tela de membros), layout sempre alinhado à esquerda em containers `max-w-4xl`/`max-w-6xl` com muito espaço em branco não-intencional à direita em telas largas.

**Avaliação honesta**: isto é um shadcn "unstyled starter" com Tailwind cru por cima — funcional, acessível na base (Radix/@base-ui cuidam disso), mas sem nenhuma identidade visual, sem calor, sem hierarquia, e com aplicação inconsistente mesmo dos poucos padrões que existem. Está coerente com a percepção do usuário ("front está simples e o design desatualizado").

**Objetivo deste documento**: mapear com precisão o que existe (tokens, componentes, rotas, telas por feature, padrões recorrentes) para permitir planejar um redesign com linguagem visual estilo Claude/Anthropic — paleta neutra quente com um acento de marca, tipografia com hierarquia real, componentes shadcn completos, bordas/sombras sutis, cantos arredondados-mas-não-bolha, densidade de informação cuidada, estados vazios/loading tratados como parte do produto.

---

## 2. Stack e fundação técnica atual

### 2.1 Stack confirmada (via `apps/web/package.json`)

| Camada | Biblioteca |
|---|---|
| Framework | React 18.3 + TypeScript, Vite 5, react-router-dom 6 (data router / `createBrowserRouter`) |
| Estilo | Tailwind CSS 3.4 (sem plugins extras — nem `@tailwindcss/forms`, nem `@tailwindcss/typography`) |
| Componentes base | shadcn/ui (CLI `shadcn` 4.11) sobre `@base-ui/react` (não Radix puro, embora ADR-006 cite Radix — `@radix-ui/react-label` e `@radix-ui/react-slot` também estão presentes como resíduo) |
| Variantes de componente | `class-variance-authority` (cva) + `tailwind-merge` + `clsx` (via helper `cn` em `@features/lib/utils`) |
| Formulários | `react-hook-form` + `@hookform/resolvers` + `zod` |
| Server state | `@tanstack/react-query` 5.51 |
| Client state | `zustand` 4.5 (`store/authStore`) |
| Gráficos | `recharts` 3.8 (usado só em `DashboardCharts`, `LastTaskGradeChart`) |
| Ícones | `lucide-react` |
| Fonte | `@fontsource-variable/geist` (Geist Variable) — única fonte, sem fonte de heading distinta (`--font-heading` aponta pro mesmo `--font-sans`) |
| Animação | `tw-animate-css` (usado nas transições de dialog/sheet/popover) |

Não há Storybook, não há Chromatic, não há testes visuais/snapshot de UI — só testes de comportamento com `@testing-library/react` + `vitest` + `msw`.

### 2.2 Tokens de design atuais (`apps/web/src/index.css` + `tailwind.config.js`)

Todos os tokens são CSS vars em `oklch(L C H)` mapeadas para classes Tailwind via `tailwind.config.js`. **O ponto crítico: C (chroma) é `0` em absolutamente todos os tokens neutros/primários**:

```
--background: oklch(1 0 0)              /* branco puro */
--foreground: oklch(0.145 0 0)          /* quase preto */
--primary: oklch(0.205 0 0)             /* cinza muito escuro — não é uma cor de marca */
--primary-foreground: oklch(0.985 0 0)
--muted: oklch(0.97 0 0)
--muted-foreground: oklch(0.556 0 0)
--border / --input: oklch(0.922 0 0)
--ring: oklch(0.708 0 0)
--secondary / --accent: oklch(0.97 0 0)
--card / --popover: oklch(1 0 0)
```

Únicas cores com chroma real (baixo) são semânticas de status:
```
--destructive: oklch(0.577 0.245 27.325)  /* vermelho */
--success: oklch(0.6 0.15 145)            /* verde */
--warning: oklch(0.75 0.16 85)            /* âmbar */
```
Essas também são as únicas usadas nos gráficos indiretamente (`DashboardCharts.tsx` usa hex hardcoded `#2563eb` e `#16a34a`, **fora do sistema de tokens** — mais uma inconsistência).

- **Radius**: `--radius: 0.625rem` (10px) mapeado em `lg`/`md`/`sm` (10px/8px/6px). Não há escala `xl`/`2xl`/`full` customizada além do que o Tailwind já traz.
- **Espaçamento**: nenhuma extensão no `tailwind.config.js` — 100% escala default do Tailwind, usada de forma ad-hoc (`p-3`, `p-4`, `p-6`, `gap-2`, `gap-4`, `gap-6` conforme o gosto de quem escreveu o arquivo).
- **Sombra**: praticamente não usada. `sheet` usa `shadow-lg`; dialogs/popovers usam `ring-1 ring-foreground/10` (contorno, não sombra); cards de página usam só `border`, sem `shadow-sm`. Não há escala de elevação definida.
- **Modo escuro**: existe um bloco `.dark { ... }` completo em `index.css` com os mesmos tokens invertidos, mas **não há nenhum toggle de tema na aplicação** (nenhum hook/estado de "dark mode" encontrado em `store/` ou `components/`) — é CSS morto hoje.
- **Fonte de heading**: `--font-heading: var(--font-sans)` — ou seja, o sistema já tem a variável semântica para uma fonte de display diferenciada, mas ela está setada para ser idêntica ao corpo. É o único ponto de tokens já preparado para uma futura hierarquia tipográfica mais expressiva.

### 2.3 Componentes prontos para reaproveitar

`apps/web/src/components/ui/` (apenas 5 arquivos):

| Componente | Observação |
|---|---|
| `button.tsx` | Bem construído: cva com variantes `default/outline/secondary/ghost/destructive/link` e tamanhos `default/xs/sm/lg/icon/icon-xs/icon-sm/icon-lg`. Base sólida para evoluir (só trocar tokens de cor). |
| `dialog.tsx` | Completo (Root/Trigger/Content/Header/Footer/Title/Description), footer já tem tratamento de fundo `bg-muted/50` + borda superior — bom precedente de "footer diferenciado" a generalizar. |
| `alert-dialog.tsx` | Completo, inclusive `AlertDialogMedia` (slot de ícone) não utilizado em nenhum lugar do app hoje. |
| `sheet.tsx` | Completo, 4 lados, mas underused (só 1 uso real: `SubmissionListDrawer`; existe uma segunda implementação de drawer manual em paralelo — ver seção 5). |
| `popover.tsx` | Completo, único uso: `NotificationBell`. |

Todos seguem o mesmo padrão de qualidade (data-slot, cva onde aplicável, `cn()` helper) — a fundação técnica dos primitivos é boa, só faltam **membros da família** e uma paleta com identidade.

### 2.4 O que precisa ser criado do zero

Nenhum destes existe em `components/ui/` hoje, apesar de serem usados constantemente via HTML cru nas features:
`card`, `input`, `textarea`, `select`, `label`, `form` (wrapper RHF), `table`, `badge`, `tabs`, `avatar`, `tooltip`, `skeleton`, `separator`, `dropdown-menu`, `toast`/`sonner`, `progress`, `checkbox`/`radio` (não usados ainda, mas prováveis).

---

## 3. Inventário de rotas e personas

Roles existentes no sistema (via `useAuthStore` / `ProtectedRoute roles=[...]`): **ADMIN_ORG**, **GESTOR**, **PROFESSOR**, **ALUNO**.

| Rota | Componente | Persona | Shell |
|---|---|---|---|
| `/login` | `LoginPage` | Pública | Sem AppShell |
| `/register` | `RegisterPage` | Pública | Sem AppShell |
| `/confirm-email` | `ConfirmEmailCallbackPage` | Pública | Sem AppShell |
| `/forgot-password` | `ForgotPasswordPage` | Pública | Sem AppShell |
| `/reset-password` | `ResetPasswordPage` | Pública | Sem AppShell |
| `/invitations/:token/accept` | `AcceptInvitePage` | Convidado (pré-login) | Sem AppShell |
| `/organizations/new` | `CreateOrganizationPage` | Autenticado sem org (fluxo de setup) | Protegido, sem AppShell |
| `/` | `RootRedirect` | Todos autenticados | Redireciona p/ `/classrooms` ou `/organizations/new` |
| `/organizations/:id` | `OrganizationRoute` → `AdminDashboard` \| `GestorDashboard` \| `StudentDashboard` \| `OrganizationDashboardPage` (fallback p/ PROFESSOR) | ADMIN_ORG / GESTOR / ALUNO / PROFESSOR (fallback) | AppShell |
| `/classrooms` | `ClassroomListPage` | Todos (criar/gerenciar só ADMIN_ORG/GESTOR) | AppShell |
| `/classrooms/:id` | `ClassroomDetailPage` + `AnnouncementFeed` slot | Todos | AppShell |
| `/curriculum` | `SubjectListPage` | Todos (gerenciar só ADMIN_ORG/GESTOR) | AppShell |
| `/curriculum/:subjectId` | `SubjectDetailPage` + `ProfessorDashboard` slot (se PROFESSOR) | Todos, dashboard extra p/ PROFESSOR | AppShell |
| `/assessment/tasks` | `TaskListPage` | PROFESSOR, ADMIN_ORG, GESTOR | AppShell, `ProtectedRoute roles` |
| `/assessment/student-tasks` | `StudentTaskListPage` | ALUNO | AppShell, `ProtectedRoute roles` |
| `*` | Redirect `/` | — | — |

Observação: a persona é resolvida **dentro** do componente de rota (`if (role === 'ADMIN_ORG')...`) em vez de rotas dedicadas por papel — funcional, mas mistura branching de apresentação com roteamento (relevante para o redesign: dá pra decidir manter esse padrão ou migrar para rotas nomeadas por persona).

---

## 4. Inventário detalhado por feature

### 4.1 `auth` — Autenticação
Telas: `LoginPage`/`LoginForm`, `RegisterPage`/`RegisterForm`, `ForgotPasswordPage`, `ResetPasswordPage`, `ConfirmEmailCallbackPage`/`EmailConfirmationPage`, `ResendConfirmationForm`.

- Todas as telas são cards centralizados `max-w-sm`/`max-w-md` com `border p-6/p-8 shadow-sm`, título `text-2xl font-semibold`, sobre fundo branco puro (`bg-background`).
- **100% inputs/botões HTML nativos** com classes repetidas em cada arquivo (`w-full rounded border px-3 py-2 text-sm`), nenhum reaproveita `components/ui`.
- Mensagens de sucesso/erro são divs `border-success/30 bg-success/10` ou texto `text-destructive` solto — sem componente `Alert`.
- Screenshot `rf13-login.png`/`rf13-login2.png` vieram em branco (captura falhou/timing), então a validação visual de login não pôde ser confirmada por imagem, mas o código deixa claro que é um cartão minimalista sem nenhum elemento de marca (sem logo, sem ilustração, sem cor).

### 4.2 `classroom` — Turmas
Telas: `ClassroomListPage` (tabela), `ClassroomDetailPage`, `ClassroomMembersPanel`, `ClassroomFormDialog`, `AddMemberDialog`, `JoinClassroomForm`.

- Listagem usa `<table>` HTML cru com `thead bg-muted`, linhas `border-t hover:bg-muted/50` — não há paginação, busca ou filtro visíveis.
- Badge de status (Ativa/Arquivada) é uma `span` com classe condicional inline, repetida quase idêntica em 3 arquivos diferentes (`ClassroomListPage`, `ClassroomDetailPage`, `ClassroomMembersPanel` para papel) — candidato óbvio a um componente `Badge` único.
- Detalhe da turma (visto em `rf15-*.png`) é uma pilha de 3 cards com borda fina: dados da turma, membros (tabela sem estilo, cabeçalho com apenas `border-b`, sem `thead` com fundo), mural de avisos. Bastante "formulário admin" no visual, pouco produto educacional.
- Código de convite usa fonte `font-mono tracking-widest` — único uso de tratamento tipográfico diferenciado no app inteiro.
- Dialogs de criar/editar/adicionar membro usam `Dialog` do `components/ui`, mas inputs internos continuam HTML cru.

### 4.3 `curriculum` — Disciplinas / Conteúdo
Telas: `SubjectListPage` (tabela), `SubjectDetailPage`, `TopicList` (accordion manual), `ContentCard`, `SubjectFormDialog`, `TopicFormDialog`, `ContentFormDialog`, `AssignTeacherDialog`, `LinkClassroomDialog`.

- Confirmado visualmente em `rf10-subject-detail.png`: header com ícone + título, botão escuro "Novo Tópico" no canto, lista de tópicos como accordion (`ChevronDown`/`ChevronRight` manual, sem componente `Accordion`/`Collapsible`), cada tópico é uma seção com borda, contadores "X item(s)" em texto pequeno, ações (editar/excluir/+) alinhadas à direita só em ícone sem label — baixa affordance para usuário novo.
- `ContentCard` tem ícone por tipo de conteúdo (vídeo/documento/link/arquivo) com `lucide-react`, badge de tipo em `bg-muted px-1.5 py-0.5` — é o componente mais "card de produto" do app, mas ainda monocromático.
- `ContentFormDialog` tem um seletor de tipo como botões pill manuais (`border-primary bg-primary` quando ativo) — outro padrão de "toggle group" reinventado que poderia ser um componente `ToggleGroup`/`Tabs`.
- Edição de conteúdo tipo arquivo está deliberadamente desabilitada com uma mensagem informativa dentro de um `Dialog` — bom exemplo de estado "não suportado" tratado de forma ao menos explícita, mas sem nenhum tratamento visual (só texto).

### 4.4 `assessment` — Tarefas e submissões
Telas: `TaskListPage` (professor), `StudentTaskListPage` (aluno), `TaskFormDialog`, `SubmissionFormDialog`, `SubmissionListDrawer` (Sheet), `EvaluationDialog`, `GradeFeedbackDrawer` (drawer manual paralelo).

- Confirmado em `verify-rf11-tasklist.png`, `rf13-tasks*.png`: lista de tarefas como pilha de cards `border p-4` com `hover` implícito, status em texto colorido inline (`text-success`/`text-warning`, não badge), botões de ação secundários (Publicar/Ver Submissões) como `border` outline pequenos.
- `StudentTaskListPage` tem o único `StatusBadge` "de verdade" do app (badge com ícone + `rounded-full`, 4 estados: não enviado, expirado, aguardando avaliação, avaliado) — é o componente com melhor tratamento de estado do sistema inteiro e um bom candidato a virar o `Badge` genérico.
- `EvaluationDialog` (visto em `rf13-eval-dialog.png`): dialog centralizado, textarea de feedback, input de nota — visual de formulário admin puro, nenhuma ligação visual com a resposta do aluno mostrada acima (mesma cor de fundo `bg-muted/40` genérica para "citação").
- `SubmissionListDrawer` usa corretamente o `Sheet` compartilhado (confirmado em `rf14-verify-drawer.png`, `rf13-drawer.png`, `rf13-submitted-fresh.png`) — layout de linha por submissão com botão "Avaliar" inline.
- **`GradeFeedbackDrawer` reimplementa um drawer do zero** (`fixed inset-0 bg-black/40` overlay + `fixed inset-y-0 right-0 ... shadow-xl` painel), ao invés de reusar `Sheet` — inconsistência de implementação que também guarda um bug-prone: não tem as animações de entrada/saída do `Sheet`, não fecha com Escape/foco-trap dos primitivos Base UI.
- Nota exibida em destaque (`text-3xl font-bold`) é o único lugar do app com tipografia de "número grande hero" — outro precedente bom pra generalizar em telas de métrica.

### 4.5 `dashboard` — Painéis por persona
Telas: `StudentDashboard`, `GestorDashboard`, `AdminDashboard`, `ProfessorDashboard` + ~15 subcomponentes de cartão/lista/gráfico (`MetricsCards`, `DashboardCharts`, `ClassroomHealthCards`, `AtRiskStudentsList`, `ActivityFeed`, `PeriodSelector`, `PendingEvaluationsBadge`, `RecentGradesList`, `SubjectAverageGradesList`, `StudentAverageGradesList`, `StudentsWithoutSubmissionList`, `SubmissionStatusSummary`, `UpcomingTasksList`, `LastTaskGradeChart`).

- **Esta é a feature mais visualmente "achatada" do produto** — confirmado em `rf20-student-dashboard.png` e `rf18-gestor-dashboard.png`: métricas em cards `border p-4` sem sombra, sem ícone de contexto na maioria, número grande `text-2xl font-semibold` + label pequeno acima — e depois listas que são só `<ul>` com `border-b` entre itens, **sem nenhum card, sem alternância de fundo, sem paginação, parecendo uma tabela de dados crua** (ex.: "Próximas tarefas", "Últimas notas e feedbacks", "Média por disciplina" no dashboard do aluno são 3 listas idênticas visualmente, uma embaixo da outra, sem separação clara de seção além de um `<h3>` pequeno).
- `MetricsCards` (admin) é grid `grid-cols-2 sm:grid-cols-4` de cards — o layout de KPI mais "dashboard de verdade" do app, mas ainda sem ícone, sem cor, sem indicador de tendência.
- `DashboardCharts` usa `recharts` com cores hex hardcoded (`#2563eb`, `#16a34a`) fora da paleta de tokens — inconsistência direta com o resto do app que é grayscale.
- `GestorDashboard`/`AdminDashboard` têm botão "Exportar PDF" como outline simples no canto — ação importante tratada com baixa hierarquia visual.
- `PeriodSelector` mistura botões de atalho (7/30/90 dias) com um mini-formulário de datas — funcional, mas sem componente de date-range picker, tudo em `<input type="date">` nativo do browser (aparência varia por SO/browser, quebra a consistência visual entre plataformas).
- Confirmado no screenshot: painel do gestor tem MUITO espaço em branco não intencional (conteúdo ocupa uma fração pequena da largura em telas maiores, sem grid responsivo pensado para preencher).

### 4.6 `communication` — Mural e notificações
Telas: `AnnouncementFeed`, `AnnouncementCard`, `AnnouncementForm`, `NotificationBell` (Popover), `NotificationPanel`.

- Confirmado em `rf15-*.png`: mural de avisos é uma pilha de cards `border p-4`, texto do aviso em `whitespace-pre-wrap text-sm`, anexos como lista de links pequenos com ícone, timestamp em `text-xs text-muted-foreground` no rodapé — nenhuma distinção visual de autor (sem avatar/nome), nenhum destaque para avisos recentes/importantes.
- `NotificationBell` é o único elemento com badge de contagem "flutuante" (`bg-destructive` circular sobre o ícone) — bom padrão a formalizar como `Badge` variant `dot`/`count`.
- `NotificationPanel` dentro do Popover é uma lista simples com indicador de não-lido (`bg-primary` dot + negrito) — de novo, sem avatar/ícone por tipo de notificação, tudo texto.

### 4.7 `organization` — Organização
Telas: `CreateOrganizationForm`/`CreateOrganizationPage`, `OrganizationDashboardPage` (fallback para PROFESSOR).

- `CreateOrganizationForm`: mesmo padrão de card centralizado dos formulários de auth — reforça que esse é o "template de tela pública" do app hoje (bom, é consistente; mas o template em si é pobre).
- `OrganizationDashboardPage` (fallback quando o papel não é ADMIN_ORG/GESTOR/ALUNO, ou seja hoje só PROFESSOR sem subjectId): é a tela mais rala do app — título + 1 card de navegação para "Turmas" com ícone. Praticamente uma tela vazia/placeholder.

### 4.8 `invitation` — Convites
Telas: `AcceptInvitePage`.

- Card centralizado (mesmo template), com estados de loading (`Loader2` girando), erro (convite inválido, ícone `XCircle` vermelho) e sucesso (ícone `UserPlus`) — é a tela com **melhor cobertura de estados** (loading/erro/conteúdo) do app, mesmo que visualmente simples, e vale como referência de estrutura ao redesenhar os outros fluxos públicos.

---

## 5. Padrões de UI recorrentes (e suas inconsistências)

| Padrão | Como é feito hoje | Inconsistência observada |
|---|---|---|
| **Cards de conteúdo/métrica** | `rounded-lg border p-4` (às vezes `p-4 space-y-3`, às vezes sem `space-y`) — sempre fundo branco, sem sombra | Nenhum componente `Card`; cada arquivo escreve a classe na mão. `AlertDialogMedia` existe mas não é usada. Nenhuma variação de card "clicável"/"destacado". |
| **Tabelas** | `<table>` HTML cru em `ClassroomListPage`, `SubjectListPage`, `ClassroomMembersPanel` | 3 implementações levemente diferentes de `thead`/`tr`/hover; sem paginação/ordenação/densidade configurável; sem estado de loading de tabela (skeleton rows). |
| **Listas simples (não-tabela)** | `<ul>` com `border-b` entre `<li>` — usado em quase toda lista de dashboard | Visualmente indistinguível de uma tabela sem cabeçalho; sem hover, sem estado vazio ilustrado. |
| **Formulários** | `react-hook-form` + `zod`, mas inputs/labels/selects/textareas são sempre HTML cru repetido (`w-full rounded border px-3 py-2 text-sm`) em ~15 arquivos diferentes | Nenhum componente `Input`/`Textarea`/`Select`/`Label`/`Form` — literalmente a mesma string de classe Tailwind copiada e colada dezenas de vezes; qualquer mudança visual exige find-replace em todo o repo. |
| **Dialogs (modais centrados)** | `components/ui/dialog.tsx`, bem usado nas features de curriculum/classroom/assessment/communication | Consistente — é o padrão mais maduro do app. Footer com `bg-muted/50` só nesse componente, não replicado em outros contextos "de encerramento de fluxo". |
| **Drawers (painel lateral)** | `components/ui/sheet.tsx` usado em `SubmissionListDrawer`; **`GradeFeedbackDrawer` reimplementa um drawer do zero** com overlay/painel manuais | Duas implementações de drawer coexistindo — divergência de animação, foco e comportamento de fechamento (Escape) entre elas. |
| **Confirmação destrutiva** | `ConfirmDialog` (wrapper de `AlertDialog`) — usado de forma consistente em classroom/curriculum/communication | Ponto positivo — é o componente compartilhado mais bem reaproveitado do código. |
| **Badges de status** | Implementados ad-hoc como `<span className="rounded(-full) px-2 py-0.5 text-xs font-medium ...">` com cor condicional inline, repetido em `ClassroomListPage`, `ClassroomDetailPage`, `ClassroomMembersPanel`, `StudentTaskListPage`, `TaskListPage`, `SubmissionListDrawer`, `GradeFeedbackDrawer` | Nenhuma padronização de variant/cor entre esses ~7 usos; `StudentTaskListPage.StatusBadge` é o mais elaborado (ícone + `rounded-full`), o resto é texto colorido simples — nenhum componente `Badge` compartilhado. |
| **Empty states** | Sempre `<p className="text-sm text-muted-foreground">Nenhum X encontrado/criado/publicado.</p>` | Zero tratamento visual — sem ícone, sem CTA contextual (ex.: "Nenhuma tarefa criada ainda" não oferece um botão de criar ali mesmo), sem ilustração. Texto muda de "Nenhuma X encontrada" para "X não disponível" sem critério aparente entre features. |
| **Loading states** | `<p>Carregando...</p>` na quase totalidade dos casos; `FullScreenLoader` (spinner `Loader2` central) só na inicialização de auth/rota protegida | Nenhum skeleton em lista/tabela/card — troca abrupta de "texto Carregando" para conteúdo, causa layout shift perceptível. |
| **Ícones** | `lucide-react`, tamanho `h-4 w-4` predominante, sempre monocromático (`text-muted-foreground` ou herda cor do texto) | Uso consistente da biblioteca (positivo), mas nunca como elemento de destaque visual — sempre utilitário pequeno ao lado de texto, nunca em círculos coloridos de contexto (avatar de tipo, ícone grande de empty state, etc.), exceto o slot `AlertDialogMedia` não usado. |
| **Navegação (Header/Sidebar)** | `Header` fixo `h-14` com nome do app em texto (`LMS Nexus`, sem logo), sino de notificação, id do usuário truncado, botão Sair; `Sidebar` `w-56` com `NavLink`s condicionais por papel | Header não tem elemento de marca real (nem cor, nem logo/wordmark tratado); sidebar tem só 3-4 itens e nenhuma seção/grupo, nenhum estado de "colapsado", nenhum indicador de página ativa além de fundo sólido `bg-primary` (que hoje é cinza escuro). |
| **Container de página** | `container mx-auto max-w-4xl p-6 space-y-6` (a maioria) ou `max-w-6xl` (dashboards de organização) ou `p-6` sem container (`TaskListPage`, `StudentTaskListPage`) | Três variações de largura máxima e presença/ausência de `container mx-auto` sem critério de persona ou tipo de conteúdo — contribui para o "espaço em branco desperdiçado" visto nos screenshots de dashboard. |

---

## 6. Gaps para um design system tipo Claude

Organizados por camada, do mais fundacional ao mais superficial:

### 6.1 Paleta de cores
- **Chroma zero em tudo** é o problema #1. Precisa de uma paleta neutra **quente** (tons levemente âmbar/bege como base, não cinza puro de `oklch` com C=0) mais uma cor de acento de marca aplicada a `--primary`/`--ring`/estado ativo — hoje `--primary` é literalmente cinza-quase-preto, então botões primários, links, itens ativos da sidebar e foco de campo não têm nenhuma cor de identidade.
- Cores semânticas (`success`/`warning`/`destructive`) existem mas têm chroma baixo (`0.15`–`0.25`) — dá pra manter a intenção mas vale revisar contra critérios de contraste AA em fundo colorido leve (`/10`, `/20` opacity) que é o padrão usado hoje.
- `DashboardCharts.tsx` usa hex hardcoded fora do sistema de tokens — precisa migrar para tokens (`--chart-1`..`--chart-5` já existem em `index.css` mas não são usados em lugar nenhum do código real).
- Modo escuro já tem tokens completos em `index.css` mas está morto (sem toggle) — decidir se entra no escopo do redesign ou fica pra depois, mas os tokens dark precisam ser revisados junto com a nova paleta clara.

### 6.2 Tipografia
- Uma fonte só (Geist Variable) para tudo, com `--font-heading` idêntico a `--font-sans` — falta decidir se heading continua Geist (com pesos/tracking diferenciados) ou ganha uma serif/display para títulos grandes (mais “editorial”, no espírito Claude.ai).
- Escala de tamanho é essencialmente binária hoje: `text-2xl` (títulos de página) / `text-sm` (corpo) / `text-xs` (metadados) — falta uma escala com mais degraus (display, h1–h3, body, caption) e regras de quando usar cada um.
- Nenhum uso de `font-heading` de fato diferenciado, nenhum tracking customizado além do `tracking-widest` usado só no código de convite.

### 6.3 Espaçamento e densidade
- Sem tokens de espaçamento no `tailwind.config.js` — todo `p-4`/`p-6`/`gap-4` é escolha individual por arquivo. Precisa de uma escala documentada (ex.: 4/8/12/16/24/32) e diretriz de quando cada componente usa qual.
- Densidade de informação nos dashboards está baixa (muito espaço vazio) mas ao mesmo tempo as listas internas estão "grudadas" (`border-b` sem padding vertical generoso) — o oposto do que se quer: mais densidade estruturada com cards, menos espaço vazio "acidental" de layout.

### 6.4 Elevação / bordas
- Sistema hoje é 100% "borda fina cinza, zero sombra" nos cards de página; dialogs/popovers usam `ring-1 ring-foreground/10` (contorno sutil) — não existe uma escala de elevação (`shadow-xs/sm/md/lg`) aplicada com intenção. Um visual "Claude" tende a usar sombras muito sutis e bordas quase invisíveis em vez de bordas sempre visíveis — vale revisar se a borda-em-tudo atual deve virar sombra-em-alguns-contextos.
- Radius atual (10px base) está numa faixa razoável para "arredondado mas não bolha" — provavelmente reaproveitável, mas os componentes secundários (`badge`, `input`, `select`) inexistentes precisam herdar a mesma escala (`sm`/`md`/`lg`) já definida.

### 6.5 Componentes shadcn faltando (confirmado por ausência em `components/ui/`)
`Card`, `Input`, `Textarea`, `Select`, `Label`, `Form` (wrapper RHF), `Table`, `Badge`, `Tabs`, `Avatar`, `Tooltip`, `Skeleton`, `Separator`, `DropdownMenu`, `Toast`/`Sonner`, `Progress`, `Accordion`/`Collapsible` (hoje reimplementado manualmente em `TopicList`), `ToggleGroup` (hoje reimplementado manualmente em `ContentFormDialog`).

Cada um desses, uma vez criado, elimina uma reimplementação manual já existente em algum lugar do código (ver seção 5) — ou seja, não é greenfield puro: dá pra extrair o padrão já usado e formalizar.

### 6.6 Estados vazios/loading/erro
- Nenhum skeleton em nenhuma tela — prioridade alta porque toda tela do produto tem pelo menos uma chamada de rede (`useQuery`) no caminho crítico.
- Empty states são só texto — sem ícone, sem CTA, sem ilustração leve. Especialmente ruim em telas centrais como lista de turmas/disciplinas/tarefas vazias (primeira experiência de um usuário novo).
- Mensagens de erro genéricas ("Não foi possível carregar o dashboard") sem ação de retry visível em nenhum lugar.

### 6.7 Densidade de informação e hierarquia visual
- Dashboards (a superfície mais "gerencial" do produto, usada por GESTOR/ADMIN_ORG/PROFESSOR) são hoje a área visualmente mais fraca: KPIs sem ícone/cor/tendência, listas sem cards, gráficos com cor fora do sistema, muito espaço vazio em telas largas.
- Falta indicação visual de papel do usuário logado (hoje só aparece um UUID truncado no header) — um `Avatar` com iniciais + nome + badge de papel mudaria a percepção de "produto cru" imediatamente.

### 6.8 Ícones e marca
- Header sem logo/wordmark estilizado (`LMS Nexus` é texto puro `text-sm font-semibold`).
- Ícones lucide usados só como utilitário monocromático pequeno — nunca como elemento de destaque (círculo colorido, tamanho maior em empty state, avatar de tipo de conteúdo).

### 6.9 Navegação
- Sidebar fixa sem seções/grupos, sem estado colapsável, sem indicação de breadcrumb nas páginas de detalhe (ex.: dentro de uma disciplina não há trilha "Disciplinas > Física") — só um botão de voltar com seta.

---

## 7. Recomendações priorizadas

Pensado como sequência de maior impacto visual / menor esforço → mais profundo, do jeito que normalmente se ataca uma fundação de design system: **tokens primeiro, depois componentes compartilhados, depois telas por feature.**

### Fase 1 — Fundação de tokens (maior alavancagem, menor esforço)
1. **Reformular a paleta em `index.css`**: sair de `oklch(x 0 0)` (chroma zero) para uma base neutra quente + 1 cor de acento de marca aplicada a `--primary`/`--ring`/estados ativos. Isso sozinho já muda a percepção de "produto genérico" em todas as ~50 telas, porque `--primary` é referenciado em botões, links, badges, sidebar ativa, foco de campo.
2. **Migrar `DashboardCharts.tsx`** dos hex hardcoded para os tokens `--chart-1..5` já existentes (mas mortos) em `index.css`, alinhados com a nova paleta.
3. **Definir escala tipográfica** (display/h1/h2/h3/body/caption) e decidir o papel de `--font-heading` (mesma fonte com peso diferenciado, ou fonte própria) — documentar e aplicar pelo menos nos títulos de página (`text-2xl font-semibold` hoje é usado indiscriminadamente para tudo).
4. **Adicionar escala de espaçamento** ao `tailwind.config.js` (mesmo que só documentada em convenção, já ajuda) e uma escala de elevação (`shadow-xs/sm/md`) para substituir gradualmente "borda em tudo".

### Fase 2 — Componentes compartilhados (fecha o gap mais visível de reuso)
5. **Criar `Card`, `Input`, `Textarea`, `Select`, `Label`, `Badge`** — nessa ordem, porque são os que mais eliminam código duplicado hoje (visto em ~15+ arquivos de formulário e ~7 usos de badge ad-hoc).
6. **Criar `Table`** e migrar `ClassroomListPage`/`SubjectListPage`/`ClassroomMembersPanel` — unifica os 3 padrões de tabela levemente diferentes.
7. **Criar `Skeleton`** e substituir os `<p>Carregando...</p>` nos pontos de maior tráfego (dashboards, listas de tarefas/turmas/disciplinas).
8. **Resolver a duplicidade de drawer**: migrar `GradeFeedbackDrawer` para usar `Sheet` (elimina uma implementação paralela de overlay/animação/foco).
9. **Criar `Avatar` + `Tooltip` + `DropdownMenu`** para o header (usuário logado com iniciais/nome/papel em vez de UUID truncado) e para ações secundárias em tabelas/cards.
10. **Criar `Tabs`/`ToggleGroup`/`Accordion`** e migrar os 2 lugares que já os reimplementam manualmente (`ContentFormDialog` seletor de tipo, `TopicList` accordion de tópicos).

### Fase 3 — Padronizar formulários
11. **Criar wrapper `Form`** (react-hook-form + shadcn, no padrão `FormField`/`FormItem`/`FormMessage`) e migrar progressivamente os ~15 formulários que hoje reescrevem `label` + `input` + mensagem de erro na mão — maior redução de código duplicado do projeto todo.

### Fase 4 — Estados vazios e feedback
12. **Padronizar um componente `EmptyState`** (ícone + título + descrição + CTA opcional) e aplicar nas ~10 ocorrências de "Nenhum X encontrado" — prioridade alta em telas de primeira experiência (turmas/disciplinas/tarefas vazias).
13. **Adicionar `Toast`/`Sonner`** para feedback de ações (hoje sucesso é só "dialog fecha", erro é texto vermelho estático — sem confirmação positiva de ações como salvar/publicar/excluir).

### Fase 5 — Telas por feature (aplicar a fundação já pronta)
14. **Dashboards primeiro** (`dashboard/` feature) — é a área com pior relação espaço-vazio/informação e maior valor percebido por GESTOR/ADMIN_ORG/PROFESSOR; migrar `MetricsCards`/`ClassroomHealthCards`/todas as `*List` para `Card` + `Badge` + ícones coloridos de contexto.
15. **Telas públicas de auth/convite/criar-organização** — todas compartilham o mesmo template de card centralizado; um único redesign desse template (com marca, ilustração leve, cor) se propaga para ~8 telas de uma vez.
16. **Header/Sidebar** — aplicar `Avatar`, wordmark com cor de marca, agrupamento de itens de navegação, estado ativo com a nova cor de acento.
17. **Restante das features** (classroom, curriculum, assessment, communication, invitation, organization) — aplicar a fundação já madura nesse ponto; a maior parte do trabalho aqui é troca de classes cruas pelos componentes novos, não redesenho conceitual.

---

## 8. Referências consultadas

- Tokens/config: `apps/web/tailwind.config.js`, `apps/web/src/index.css`
- Primitivos: `apps/web/src/components/ui/{button,dialog,alert-dialog,sheet,popover}.tsx`
- Shell: `apps/web/src/components/layout/{AppShell,Header,Sidebar}.tsx`, `apps/web/src/components/shared/{ConfirmDialog,FullScreenLoader,ProtectedRoute,PublicRoute}.tsx`
- Rotas: `apps/web/src/app/routes.tsx` + `app/*.tsx`
- Todas as 8 features em `apps/web/src/features/{assessment,auth,classroom,communication,curriculum,dashboard,invitation,organization}/components/`
- Decisão existente: `docs/architecture/adrs/ADR-006-shadcn.md` (nenhuma decisão de paleta/tipografia registrada — este documento é o primeiro a mapear esse gap)
- Screenshots de referência analisados visualmente: `rf10-subject-detail.png`, `rf13-login2.png` (em branco), `rf13-tasks.png`, `rf13-tasks-prof.png`, `rf13-drawer.png`, `rf13-eval-dialog.png`, `rf13-submitted-fresh.png`, `verify-rf11-tasklist.png`, `rf14-verify-drawer.png`, `rf15-student-view.png`, `rf15-prof-publish-success.png`, `rf18-gestor-dashboard.png`, `rf20-student-dashboard.png`
