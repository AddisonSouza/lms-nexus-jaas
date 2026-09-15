# [fix] aluno não vê tarefas em Minhas Tarefas (schema my-grades)

## Objetivo
Fazer "Minhas Tarefas" listar as tarefas publicadas para o aluno. Hoje a página
mostra "Nenhuma tarefa disponível" mesmo com tarefas publicadas (RF-12, RF-14).

## Comportamento esperado
- `GET /tasks/my-grades` responde 200 sem `updatedAt`; o parse Zod de
  `taskWithGradeSchema` exige o campo e falha em silêncio. Corrigir só no front.
- Falha de carga mostra estado de erro com "Tentar de novo", não lista vazia.
- Aluno consegue enviar resposta, editar antes do prazo e abrir nota/feedback.

## Fora do escopo
- Regra de negócio de submissão no backend (validada e ok via API).
- Mudanças no contrato de `GET /tasks/my-grades`.
