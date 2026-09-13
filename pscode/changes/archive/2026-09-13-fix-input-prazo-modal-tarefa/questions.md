# Grill Me

- [x] Como deve ficar o campo Prazo no modal? — **Data + hora separados**: dois
  campos nativos lado a lado (`type="date"` dd/mm/aaaa e `type="time"` HH:mm, sem
  segundos), combinados em um único `deadline`. Sem dependência nova.
- [x] Como o usuário informa a hora? — **Campo de hora livre** (24h, qualquer
  minuto válido), sem lista de horários pré-definidos.
- [x] Vira componente reutilizável? — **Sim**, em `components/ui/` (controlável
  por react-hook-form), para outras telas com data/hora não duplicarem a lógica.
- [x] Hora preenchida sozinha ao escolher a data? — **Sim, 23:59** quando a hora
  ainda estiver vazia; o usuário pode trocar.
