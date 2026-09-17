# [fix] tarefa não fecha após o prazo

**Severidade:** alto · RF-11 · origem: bateria E2E 15/09/2026

## Objetivo

Fazer a tarefa evoluir para `CLOSED` quando o prazo expira. Hoje ela continua
`PUBLISHED` para sempre; só a submissão é bloqueada com 422.

## Comportamento esperado

- Status `CLOSED` derivado do prazo (na leitura ou por job), refletido em
  `GET /tasks`, `GET /tasks/published` e `GET /tasks/my-grades`.
- Badge "Encerrada" na lista do professor e na do aluno.

## Fora do escopo

- Transição para `GRADED`.
- Reabertura de prazo.
