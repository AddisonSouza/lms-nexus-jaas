## Context

`apps/web` usa um tema shadcn genérico: tokens de cor em OKLCH neutros, uma única fonte (Geist Variable), raio de borda único (`--radius`), e apenas 5 primitivos em `components/ui/` (`button`, `alert-dialog`, `dialog`, `popover`, `sheet` — sem `card`/`input`/`badge`/`table`/`textarea`). `tailwind.config.js` mapeia essas cores via `hsl(var(--x))`, mas os valores das variáveis já estão em OKLCH — um bug latente (a função `hsl()` recebe uma string que não é HSL). Páginas de feature (`features/*/components/*Page.tsx`) escrevem Tailwind cru inline (`rounded-lg border p-4`, tabelas manuais, badges manuais via template string) em vez de reusar primitivos compartilhados. Dark mode tem tokens `.dark` completos no CSS, mas nenhum `ThemeProvider`/toggle os ativa (confirmado em `app/providers.tsx`, que documenta isso explicitamente em comentário).

O usuário importou do Claude Design um protótipo chamado "Organic" (`LMS Nexus.dc.html` + `organic.css`): paleta cream/terracota + verde-oliva de acento secundário, tipografia de destaque Caprasimo (display) + Figtree (corpo), tudo em formato pill (botões, inputs, tags, segmented control) com sombras suaves em vez de bordas duras, e uma escala de espaçamento/raio/sombra própria. O protótipo cobre login, aceite de convite, dashboards por papel (aluno/professor/gestor/admin), lista/detalhe de turma, disciplina com tópicos, lista de tarefas e avaliação de submissões — todas telas que já existem no app real, mapeadas 1:1 nas rotas atuais (ver tabela em `proposal.md`/plano aprovado).

## Goals / Non-Goals

**Goals:**
- Substituir a fundação de tokens de design (cor, tipografia, espaçamento, raio, sombra) pela paleta Organic, em tema claro e escuro, corrigindo o bug `hsl()`/OKLCH.
- Ligar dark mode de verdade: `ThemeProvider` + toggle no `Header`, persistindo em `localStorage`.
- Ampliar `components/ui/` com os primitivos que faltam (`Card`, `Badge`, `Input`, `Textarea`, `Table`, `Segmented`) e restilizar os existentes (`Button`, `Dialog`, `Sheet`, `Popover`) para o visual pill/soft-shadow.
- Reskin do shell (`AppShell`, `Header`, `Sidebar`) e de todas as páginas de feature existentes, usando os primitivos acima, sem alterar comportamento.

**Non-Goals:**
- Não implementa a tela "Membros da organização" do protótipo (lista de membros + convites pendentes) — o backend só expõe `POST /organizations/{id}/invitations` e `DELETE /organizations/{id}/members/{userId}`, sem endpoint de listagem. Requer trabalho de backend fora desta change.
- Não altera nenhum fluxo de autenticação, join-by-code, convite, ou qualquer contrato de API/schema de banco.
- Não implementa um seletor de múltiplas organizações na sidebar — o JWT (`app/store/authStore.ts`) só carrega uma `organizationId`, não há suporte a multi-org hoje.
- Não introduz nova biblioteca de UI — continua sobre Tailwind + `class-variance-authority` + `@base-ui/react`, seguindo o padrão já usado em `button.tsx`.

## Decisions

**1. Tokens em CSS custom properties, consumidos via `var()` direto (não mais `hsl(var(--x))`).**
Alternativa considerada: manter o wrapper `hsl()` e converter os hex do Organic para HSL. Rejeitada porque perpetuaria a indireção desnecessária e complicaria futuras trocas de espaço de cor; `tailwind.config.js` passa a referenciar `var(--background)` etc. diretamente, com o valor completo (incluindo função de cor) definido em `index.css`.

**2. Fontes via `@fontsource` (Caprasimo + Figtree), não link direto ao Google Fonts.**
O projeto já carrega Geist assim (`@fontsource-variable/geist`); manter o padrão evita FOUC/CSP issues de dependência externa em runtime e segue a convenção existente.

**3. Novos primitivos seguem a convenção já estabelecida em `button.tsx`** (cva + `@base-ui/react` onde aplicável, `cn()` de `@features/lib/utils`), não uma biblioteca nova. `Card`/`Badge`/`Table`/`Segmented` são componentes de apresentação simples (sem primitiva headless por trás); `Input`/`Textarea` são wrappers finos de `<input>`/`<textarea>`.

**4. Botões/inputs/tags viram pill (`rounded-full`) no nível do componente, não via token de raio genérico.**
O Organic usa raio grande (`--radius-lg`) em cards/diálogos mas pill em controles interativos — não dá para expressar isso com uma única escala `sm/md/lg` de `border-radius`. Cards/diálogos usam a escala de tokens; botões/inputs/tags/segmented aplicam `rounded-full` diretamente na classe do componente.

**5. Reskin de página é uma substituição de markup, não uma reescrita de lógica.**
Cada página de feature troca `<div className="rounded-lg border p-4">`/tabelas manuais/badges manuais pelos novos primitivos, mantendo hooks, chamadas TanStack Query, Zod schemas e query keys existentes intactos. Nenhum hook novo de dados é necessário.

**6. Ordem de execução em fases** (fundação → primitivos → shell → features), cada fase é um conjunto de tasks que pode ser mergeado e testado isoladamente antes da próxima, minimizando o risco de uma mudança de token quebrar tudo de uma vez.

## Frontend — componentes, hooks e query keys afetados

Nenhum hook, schema Zod ou query key é criado, removido ou alterado em contrato — o reskin é de apresentação. Componentes afetados (assinatura/props preservadas onde já existem; novos primitivos ganham API própria mínima):

- **Fundação**: `src/index.css`, `tailwind.config.js`, novo `src/components/layout/ThemeProvider.tsx` (ou equivalente), `Header.tsx` (adiciona toggle).
- **Primitivos novos**: `src/components/ui/card.tsx`, `badge.tsx`, `input.tsx`, `textarea.tsx`, `table.tsx`, `segmented.tsx`.
- **Primitivos restilizados**: `button.tsx`, `dialog.tsx`, `sheet.tsx`, `popover.tsx`, `alert-dialog.tsx`.
- **Shell**: `Sidebar.tsx`, `Header.tsx`, `AppShell.tsx`.
- **Features (markup apenas)**: `features/auth/components/*`, `features/organization/components/*`, `features/classroom/components/*`, `features/curriculum/components/*`, `features/assessment/components/*`, `features/communication/components/*`, `features/dashboard/components/*`, `features/invitation/components/*`.

Backend: nenhum arquivo em `apps/api` é tocado. Nenhuma migration Flyway. Nenhum Port novo/modificado.

## Risks / Trade-offs

- **[Risco]** Trocar tokens globais pode quebrar visualmente componentes que hardcodam cores fora do sistema de tokens (ex.: gráficos em `DashboardCharts.tsx`/`LastTaskGradeChart.tsx` que talvez usem hex fixos). → **Mitigação**: revisar esses componentes na fase de Dashboards e trocar cores fixas pelos tokens `--color-accent`/`--color-accent-2`.
- **[Risco]** Testes de FE (`.test.tsx`) que fazem assert sobre classes CSS específicas ou estrutura de DOM podem quebrar ao trocar markup por primitivos. → **Mitigação**: rodar Vitest após cada fase; ajustar apenas asserções de classe/estrutura, nunca lógica de teste de comportamento.
- **[Risco]** `rounded-full` em inputs longos (ex.: textarea, campos de formulário maiores) pode ficar visualmente estranho fora do padrão do protótipo (que usa pill só em campos de uma linha). → **Mitigação**: seguir o próprio protótipo, que já usa `border-radius: var(--radius-md)` em `textarea.input` (não pill) — replicar essa exceção no primitivo `Textarea`.
- **[Trade-off]** Migrar `hsl(var(--x))` para `var(--x)` direto no `tailwind.config.js` é uma mudança mecânica, mas qualquer lugar do código que hoje component customização de opacidade via `hsl(var(--x) / 0.5)` (padrão comum em Tailwind) para de funcionar, porque a variável passa a ser uma função de cor completa, não uma tripla de componentes. → **Mitigação**: grep por `/ ` (slash opacity) sobre as variáveis de cor antes de migrar e ajustar caso existam usos.

## Migration Plan

1. Fase 1 (Fundação): tokens + fontes + `tailwind.config.js` + `ThemeProvider`/toggle. Mergeável isoladamente — não quebra nada porque nenhum componente muda ainda.
2. Fase 2 (Primitivos): novos/restilizados `components/ui/*`. Sem uso ainda pelas páginas — mergeável isoladamente.
3. Fase 3 (Shell): `AppShell`/`Header`/`Sidebar` passam a usar os primitivos. Primeira mudança visível ponta a ponta.
4. Fase 4 (Features): reskin por área (auth → dashboards → turmas/disciplinas → tarefas/avaliação), cada uma testável e revisável de forma incremental.

Rollback: como é um único branch/PR com commits por fase, reverter uma fase problemática é um `git revert` do(s) commit(s) daquela fase, sem afetar as fases anteriores já mergeadas.

## Open Questions

- Nenhuma pendente — escopo, tela "Membros" e dark mode já foram decididos explicitamente com o usuário antes desta change.
