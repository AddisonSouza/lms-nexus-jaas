# Diálogos não fecham após sucesso (avaliar, adicionar membro)

## Summary
Ao avaliar uma submissão ou adicionar um membro à turma, a operação é salva, mas
o diálogo continua aberto e a lista só atualiza com reload. O professor clica de
novo e recebe um erro silencioso. Após a correção, o diálogo fecha e a lista
atualiza na hora.

## Technical detail
- Causa raiz: as respostas de escrita saem com timestamps `null`; o Zod do front
  (`z.string()`) rejeita, a mutation cai em erro e o `onSuccess` (fechar +
  invalidar) nunca roda. O front já está correto.
- Avaliar (`PATCH /submissions/{id}/evaluation`): `SubmissionMapper.toEntity`
  ignora `createdAt`; `SubmissionRepositoryImpl.save` faz `em.merge` e o null
  sobrescreve o valor na entidade gerenciada → `createdAt: null`. 2º "Salvar" →
  422 `SubmissionAlreadyEvaluated`.
- Adicionar membro (`POST /classrooms/{id}/members`):
  `ClassroomRepositoryImpl.saveMember` devolve o domínio de entrada, sem o
  `joinedAt` gerado no `@PrePersist` → `joinedAt: null`.
- Correção: repositórios devolvem o estado persistido (via MapStruct, sem
  mapeamento manual). Sem mudança de schema nem de contrato.
- Submit já é desabilitado por `isPending` nos dois diálogos — só cobrir com teste.

## Scope
### In
- Timestamps não nulos nas respostas de avaliar submissão e adicionar membro.
- Testes de integração da API (Testcontainers) para os dois endpoints.
- Testes de front: diálogo fecha, query invalidada e submit desabilitado quando pendente.
### Out
- Mensagem de erro da API nos diálogos (card erros-4xx-sem-feedback-na-ui).
- Seleção de membro por lista em vez de UUID (card próprio).
- Diálogos que já fecham (turma, disciplina, tópico); afrouxar schemas Zod.

## Subtasks
- [x] API: avaliação de submissão retorna `createdAt`/`updatedAt` preenchidos + teste de integração
- [x] API: adicionar membro à turma retorna `joinedAt` preenchido + teste de integração
- [x] Web: testes de `SubmissionListDrawer`/`EvaluationDialog` (fecha, invalida, submit pendente desabilitado)
- [x] Web: testes de `ClassroomMembersPanel` (fecha, invalida membros, submit pendente desabilitado)
