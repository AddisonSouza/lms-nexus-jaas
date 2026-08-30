# Mensagem interna do Zod ao adicionar membro sem papel

## Summary

Ao adicionar alguém a uma turma sem escolher o papel, o diálogo mostra
`Invalid enum value. Expected 'PROFESSOR' | 'ALUNO'` — texto interno da
biblioteca de validação — em vez de "Selecione o papel". A correção troca a
opção de mensagem do schema e passa a cobrir o caso com teste.

## Technical detail

- Em `apps/web/src/features/classroom/schemas/addMemberSchema.ts`, `role` usa
  `z.enum([...], { required_error: 'Selecione o papel' })`. O `required_error` só
  vale para valor **ausente**; o `<select>` do `AddMemberDialog` envia `""`, que
  cai em `invalid_enum_value` e usa a mensagem padrão do Zod.
- A correção é a mesma aplicada ao `inviteMemberSchema` na #139:
  `errorMap: () => ({ message: 'Selecione o papel' })`, que cobre ausente e
  inválido.
- É a **única** ocorrência do problema no front: `classroomSchema.status` e
  `contentSchema.contentType` também são `z.enum`, mas seus selects não têm opção
  vazia, então nunca enviam `""`.
- Os `<label>` do `AddMemberDialog` não têm `htmlFor` nem `id` nos campos. Além
  de ser um defeito de acessibilidade (clicar no rótulo não foca o campo),
  impede buscar o campo por label no teste. Segue o padrão do
  `InviteMemberDialog`.
- O diálogo não tem teste hoje — o arquivo é novo.

## Scope

### In

- `errorMap` no `role` do `addMemberSchema`.
- `id` + `htmlFor` nos dois campos do `AddMemberDialog`.
- Teste do `AddMemberDialog`: sem papel, sem `userId` e caminho feliz.

### Out

- O campo `userId` e a UX de colar UUID na mão para adicionar membro.
- `classroomSchema.status` e `contentSchema.contentType`, que não têm o defeito.
- Qualquer mudança no `ClassroomMembersPanel` ou nos endpoints de turma.

## Subtasks

- [x] Trocar `required_error` por `errorMap` no `role` do `addMemberSchema`
- [ ] Ligar rótulo e campo por `id`/`htmlFor` nos dois campos do `AddMemberDialog`
- [ ] Criar `AddMemberDialog.test.tsx` cobrindo sem papel, sem `userId` e o caminho feliz
