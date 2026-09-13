# [fix] input de prazo difícil de usar no modal de tarefa

## Summary

O campo **Prazo** do modal "Nova Tarefa" hoje é um `datetime-local` nativo: fora
do design orgânico e chato de operar (segundos, cursor pulando entre data e
hora). Ele passa a ser **dois campos lado a lado** — data (`dd/mm/aaaa`) e hora
(`HH:mm`, 24h, sem segundos) —, com a hora sugerida como **23:59** ao escolher a
data. O que é enviado para a API não muda.

## Technical detail

- Novo componente `apps/web/src/components/ui/datetime-field.tsx`: recebe
  `value: string` (ISO local `YYYY-MM-DDTHH:mm`) e `onChange`, renderiza um
  `<Input type="date">` e um `<Input type="time">` e devolve a string combinada.
  Emite `''` enquanto a data estiver vazia — a validação Zod atual continua
  valendo sem alteração.
- Ao preencher a data com a hora vazia, o componente sugere `23:59`; a hora
  isolada (sem data) não forma valor.
- `TaskFormDialog.tsx`: troca o `register('deadline')` por um `Controller` do
  react-hook-form em volta do novo componente. O restante do form não muda.
- `task.schema.ts` fica como está: `deadline` continua string ISO local validada
  como data futura; `LocalDateTime.parse` no `TaskResource` aceita `HH:mm` sem
  segundos, então o back-end não muda.
- O `type="time"` nativo não exibe segundos quando `step` não é definido — é o
  comportamento desejado; nada de `step` no campo.

## Scope

### In
- Componente de UI reutilizável de data + hora.
- Campo **Prazo** do modal de criação de tarefa usando o novo componente.
- Sugestão de 23:59 ao escolher a data.
- Teste unitário do componente (combinação, limpeza, sugestão de hora).

### Out
- Contrato e código do back-end (`POST /tasks` inalterado).
- Novas regras de validação de prazo.
- Outras telas/formulários com data — a adoção fica para quando surgirem.
- Calendário custom em popover e nova dependência de date picker.

## Subtasks
- [x] Criar `components/ui/datetime-field.tsx` (data + hora → ISO local, sugestão de 23:59)
- [x] Cobrir o componente com teste unitário
- [x] Usar o componente no campo Prazo do `TaskFormDialog` via `Controller`
- [x] Validar no app: criar tarefa com prazo, sem segundos, e conferir o valor salvo
