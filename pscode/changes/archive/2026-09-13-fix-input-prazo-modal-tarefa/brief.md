# [fix] input de prazo difícil de usar no modal de tarefa

## Objetivo

Tornar o campo **Prazo** do modal "Nova Tarefa" fácil e agradável de preencher.
Hoje ele é um `datetime-local` nativo: visual fora do design orgânico e chato de
operar (campos de segundos, cursor pulando entre data e hora).

## Comportamento esperado

- O prazo é informado como **data + hora/minuto**, **sem segundos**.
- Formato brasileiro: data `dd/mm/aaaa`, hora em 24h (`HH:mm`).
- Preenchimento fluido: dá para digitar ou escolher sem o cursor travar.
- Campo alinhado ao design orgânico (mesmo estilo dos demais inputs do modal).
- Validação atual preservada: prazo obrigatório e no futuro.

## Fora de escopo

- Contrato da API — o back-end continua recebendo o mesmo formato de data/hora.
- Regras de validação de prazo (além das que já existem).
- Outros formulários e telas fora do modal de criação de tarefa.
