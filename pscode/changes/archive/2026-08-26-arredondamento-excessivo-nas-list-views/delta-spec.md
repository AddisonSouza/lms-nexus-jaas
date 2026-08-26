# Arredondamento excessivo nas list views — Delta

## Changed

- **Primitivo `Card` (`apps/web/src/components/ui/card.tsx`)**: o raio passa de
  `rounded-[calc(var(--radius-lg)*1.15)]` (~32px) para `rounded-[var(--radius-md)]`
  (16px), o token que o design system já declara como default
  (`--radius: var(--radius-md)`). A mudança propaga para toda superfície de
  conteúdo que usa `Card` — list views de Turmas, Disciplinas e Tarefas, listas
  do dashboard, `AnnouncementCard`, `TopicList` e `ContentCard` — sem token novo
  e sem valor fixo.
- **Listas em tabela (`ClassroomListPage`, `SubjectListPage`)**: o `Card` que
  envolve a `Table` ganha `overflow-hidden`, para o fundo do `hover` da
  `TableRow` respeitar o arredondamento na primeira e na última linha em vez de
  vazar sobre o canto.

## Notes

- Nenhum teste assere classe de raio; suíte do `apps/web` inalterada.
- Fora de escopo e inalterados: overlays (`dialog`, `alert-dialog`, `popover`,
  `sheet`) e o canto do `AppShell`, que mantêm o raio maior; `rounded-full` de
  botões, inputs, badges e itens da sidebar.
