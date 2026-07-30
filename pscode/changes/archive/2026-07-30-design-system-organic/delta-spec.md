# design-system-organic — Delta

## Added
- Capability `organic-design-system`: tokens de design (paleta clara e escura, tipografia Caprasimo/Figtree, escalas de espaçamento, raio e sombra) expostos como custom properties CSS em `apps/web/src/index.css` e consumidos via `tailwind.config.js`.
- Dark mode real: `ThemeProvider` com toggle persistido em `localStorage`, aplicado a toda a árvore de componentes.
- Novos primitivos de UI reutilizáveis: `Card`, `Badge`, `Input`, `Textarea`, `Table`, `Segmented`.

## Changed
- `Button`, `Dialog`, `Sheet` e `Popover` restilizados para o visual pill/soft-shadow.
- Shell de navegação (`AppShell`, `Header`, `Sidebar`) e todas as páginas de feature passam a consumir os novos tokens e primitivos em vez de Tailwind cru inline.
- Corrigido o bug de `hsl(var(--x))` aplicado sobre valores já expressos em OKLCH.

## Removed
- Tokens `.dark` mortos no CSS, que existiam sem nenhum toggle que os ativasse.

## Notas
Mudança puramente de apresentação: nenhum endpoint, contrato de API, schema de banco ou regra de domínio foi alterado. Nomes de classes e estrutura de DOM de componentes internos mudaram, o que pode exigir ajuste em testes de frontend que fazem assert sobre classes específicas.
