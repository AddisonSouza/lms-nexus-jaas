# Mensagem interna do Zod ao adicionar membro sem papel

## Objetivo

O diálogo de adicionar membro à turma mostra a mensagem interna do Zod quando o
papel não é escolhido.

## Estado atual

Em `apps/web/src/features/classroom/schemas/addMemberSchema.ts`, o campo `role`
usa `z.enum(['PROFESSOR', 'ALUNO'], { required_error: 'Selecione o papel' })`.
O `required_error` só cobre valor **ausente** — o `<select>` do `AddMemberDialog`
manda `""`, que cai em `invalid_enum_value`. O usuário vê
`Invalid enum value. Expected 'PROFESSOR' | 'ALUNO'` em vez de "Selecione o papel".

## Comportamento esperado

Submeter o formulário sem escolher o papel mostra "Selecione o papel".

## Fora de escopo

- O campo `userId` e o resto do formulário.
- A UX de colar UUID na mão para adicionar membro.

## Origem

Mesmo defeito corrigido no `inviteMemberSchema` da organização na #139, onde a
solução foi `errorMap`. É a única outra ocorrência de `required_error` no front.
