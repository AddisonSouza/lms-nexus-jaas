# [fix] input de prazo difícil de usar no modal de tarefa — Delta

## Added

- **Campo de data e hora reutilizável** (`components/ui/datetime-field.tsx`):
  dois inputs nativos — data e hora (`HH:mm`, sem segundos) — que compõem um
  valor ISO local `YYYY-MM-DDTHH:mm`. Enquanto data ou hora estiverem vazias, o
  valor emitido é `''`.
- **Hora sugerida**: ao escolher a data com a hora ainda vazia, a hora é
  preenchida com `23:59`; o usuário pode trocar.

## Changed

- O campo **Prazo** do modal "Nova Tarefa" deixa de ser um `datetime-local`
  único e passa a usar o novo componente, ocupando a linha inteira do modal
  (Pontuação máxima desce para a linha seguinte).
- O **prazo exibido no card da lista de tarefas** passa de `20/09/2026, 23:59:00`
  para `20/09/2026, 23:59` — sem segundos.

## Não mudou

- Contrato do `POST /tasks`: o `deadline` continua string ISO local e é
  interpretado por `LocalDateTime.parse`.
- Validação: prazo obrigatório e data futura, como antes.
- O formato exibido (`dd/mm/aaaa` vs `mm/dd/yyyy`, 24h vs AM/PM) segue o idioma
  do navegador — comportamento nativo, decidido conscientemente.
