# [fix] publicar tarefa retorna 409 mesmo tendo publicado

## Summary

Publicar uma tarefa funciona no servidor, mas a tela não percebe: a lista
continua mostrando `DRAFT` com o botão Publicar, e o segundo clique traz um
`409` silencioso. A causa é a API devolver `createdAt: null` na resposta do
publish, o que faz o front descartar a resposta como inválida.

## Technical detail

- `PATCH /tasks/{id}/publish` responde `200` com `"createdAt": null` (verificado
  no navegador), embora o banco tenha o valor correto.
- Origem: `TaskRepositoryImpl.save()` faz `em.merge(entity)` de uma entidade
  montada por `TaskMapper.toEntity`, que tem `@Mapping(target = "createdAt",
  ignore = true)`. O banco preserva a coluna, mas o objeto devolvido volta com
  `createdAt` nulo — e `PublishTaskService` mapeia esse objeto na resposta.
- No front, `taskSchema` (`api/tasks.ts`) exige `createdAt: z.string()`. O
  `parse` lança, a mutation cai no caminho de erro e o `onSuccess` de
  `usePublishTask` — que invalida `taskKeys.lists()` — nunca roda. Daí a lista
  velha e o `409` no clique seguinte.
- `usePublishTask` não trata erro e `TaskListPage` não exibe nada: a falha é
  invisível, o que escondeu o bug.

## Scope

### In
- `TaskRepositoryImpl.save()` devolve a tarefa com os campos de auditoria
  preenchidos (vale para publish e qualquer update futuro).
- Teste de integração cobrindo `createdAt` não nulo na resposta do publish.
- Mensagem de erro inline na lista de tarefas quando a publicação falhar.

### Out
- Transições `CLOSED` e `GRADED` e outros endpoints fora de tarefa.
- Afrouxar o `taskSchema` no front — o contrato continua exigindo `createdAt`.
- Sistema de toast global.

## Subtasks
- [x] Corrigir `TaskRepositoryImpl.save()` para devolver os campos de auditoria
- [x] Teste de integração: publish devolve `createdAt` preenchido
- [x] Mostrar erro inline na lista quando a publicação falhar
- [ ] Validar no app: publicar tarefa e ver a lista virar `PUBLISHED` sem 409
