## Why

O frontend (`apps/web`) usa hoje um tema shadcn genérico, sem identidade visual própria: paleta neutra OKLCH, uma única fonte (Geist), um único raio de borda, e apenas 5 primitivos em `components/ui/` (button, alert-dialog, dialog, popover, sheet). Páginas de feature escrevem Tailwind cru inline em vez de reusar primitivos, e o dark mode tem tokens `.dark` no CSS que nunca foram ligados. O usuário importou um protótipo do Claude Design ("Organic": paleta quente cream/terracota, tipografia Caprasimo + Figtree, componentes em pill/soft-shadow) e pediu para aplicá-lo às telas já existentes de todos os módulos (identity/auth, organization, classroom, curriculum, assessment, communication), sem alterar fluxos, regras de negócio ou contratos de API.

## What Changes

- Nova fundação de design tokens (paleta clara + escura, tipografia, escala de espaçamento/raio/sombra) em `apps/web/src/index.css` e `tailwind.config.js`, corrigindo o bug atual de `hsl(var(--x))` sobre valores já em OKLCH.
- Dark mode real: `ThemeProvider` + toggle persistido em localStorage (hoje só existe CSS `.dark` morto, sem toggle).
- Novos primitivos de UI reutilizáveis: `Card`, `Badge`, `Input`, `Textarea`, `Table`, `Segmented`; restyle de `Button`, `Dialog`, `Sheet`, `Popover` existentes para o visual pill/soft-shadow.
- Restyle do shell de navegação (`AppShell`, `Header`, `Sidebar`) usando os novos primitivos.
- Restyle de todas as páginas de feature existentes (auth, dashboards por papel, turmas, disciplinas, tarefas/avaliação) para consumir os novos tokens e primitivos, sem alterar comportamento, chamadas de API ou lógica de negócio.
- **Não é BREAKING** para usuários/API — é puramente de apresentação (CSS/markup/classes). Pode alterar nomes de classes/estrutura de DOM em componentes internos, o que pode exigir ajuste em testes de FE que fazem assert sobre classes específicas.

## Capabilities

### New Capabilities
- `organic-design-system`: contrato de tokens de design (cores claro/escuro, tipografia Caprasimo/Figtree, espaçamento, raio, sombra), comportamento de alternância de tema (persistência, aplicação em toda a árvore), e o conjunto de primitivos de UI disponíveis (Button, Card, Badge, Input, Textarea, Table, Segmented, Dialog, Sheet, Popover) que as páginas de feature devem consumir.

### Modified Capabilities
(nenhuma — não há mudança de requisito de negócio em nenhum módulo; apenas apresentação visual das capacidades já existentes)

## Impact

- **Frontend apenas** (`apps/web`). Nenhum endpoint, contrato de API, schema de banco ou regra de domínio muda.
- Arquivos centrais: `src/index.css`, `tailwind.config.js`, `src/components/ui/*`, `src/components/layout/*`.
- Toca (apenas apresentação) todos os módulos de feature: `features/auth`, `features/organization`, `features/classroom`, `features/curriculum`, `features/assessment`, `features/communication`, `features/dashboard`, `features/invitation`.
- Fora de escopo: tela "Membros da organização" do protótipo (falta endpoint de listagem de membros/convites no backend), qualquer mudança de fluxo de autenticação/join, multi-org switcher (o JWT só carrega uma `organizationId`, não há suporte a múltiplas organizações hoje).
