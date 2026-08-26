## Summary

As telas de listagem estão redondas demais e destoam do design definido. O raio
das superfícies de conteúdo cai de ~32px para 16px, deixando as listas com uma
aparência mais contida e consistente entre si.

## Technical detail

- A origem é única: o primitivo `Card` (`apps/web/src/components/ui/card.tsx`)
  usa `rounded-[calc(var(--radius-lg)*1.15)]` = **32.2px**. Toda list view passa
  por ele — `ClassroomListPage`, `SubjectListPage` (Card envolvendo `Table`),
  `TaskListPage`, `StudentTaskListPage`, listas do dashboard, `AnnouncementCard`,
  `TopicList`, `ContentCard`.
- A correção é trocar esse valor por `rounded-[var(--radius-md)]` (**16px**), o
  token que o próprio design system já declara como default
  (`--radius: var(--radius-md)` em `index.css`). Nenhum token novo é criado e
  nenhum valor fixo é introduzido.
- `Card` é o único ponto alterado — não há `className` de raio sobrescrevendo o
  primitivo nas páginas, então a mudança propaga sem ajuste caso a caso.
- Efeito colateral a verificar: nas listas em tabela o `Card` recebe `p-0` e o
  `hover` das `TableRow` pinta o fundo até a borda. Sem recorte, o retângulo do
  hover vaza sobre o canto arredondado na primeira e na última linha.
- Nenhum teste atual assere classe de raio, então a mudança não quebra suíte por
  snapshot.

## Scope

### In

- Raio do primitivo `Card`: `calc(var(--radius-lg)*1.15)` → `var(--radius-md)`.
- Recorte dos cantos nas listas em tabela, para o `hover` da linha respeitar o
  arredondamento do card.
- Validação visual das telas de listagem em tema claro e escuro.

### Out

- Overlays (`dialog`, `alert-dialog`, `popover`, `sheet`), que mantêm o
  `calc(var(--radius-lg)*1.15)` atual.
- Canto do `AppShell` (`rounded-tl-[calc(var(--radius-lg)*1.4)]`), que é moldura
  do shell.
- `rounded-full` de botões, inputs, badges e itens da sidebar.
- Redesenho, layout, densidade, espaçamento e qualquer mudança funcional.


## Subtasks

- [x] Trocar o raio do primitivo `Card` para `rounded-[var(--radius-md)]` (#94)
- [x] Garantir o recorte dos cantos nas listas em tabela (hover da linha não vaza) (#95)
- [x] Rodar lint, type-check e testes do `apps/web` (#96)
- [x] Validar no browser as listas (Turmas, Disciplinas, Tarefas, dashboard) em tema claro e escuro (#97)
