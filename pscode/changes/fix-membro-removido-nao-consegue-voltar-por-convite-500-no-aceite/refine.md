# Membro removido volta à organização e à turma

## Summary

Quem foi removido de uma organização não consegue voltar: aceitar um novo convite
dá erro. O mesmo acontece, pelo código, com o aluno removido de uma turma que
tenta voltar pelo código ou ser readicionado. Esta mudança deixa a pessoa voltar,
com o papel novo e a data da volta.

## Technical detail

- **Causa:** a remoção é soft delete (`deleted_at`), e as buscas só enxergam
  vínculo ativo. Sem achar, o service grava um vínculo com id novo (insert), que
  esbarra em `uq_member (organization_id, user_id)` ou
  `uq_classroom_member (classroom_id, user_id)` — as duas ignoram `deleted_at`.
- **Correção — reativar:** antes de criar, procurar o vínculo **removido** do
  mesmo par. Se existir, reativar em vez de inserir. Sem migração.
- **Reativação é um `UPDATE` explícito** no repositório: `deleted_at = NULL`,
  `role` do convite/entrada e `joined_at = agora`. `joined_at` tem
  `updatable = false` nas duas entidades, então um `merge` não gravaria a data
  da volta.
- **Organização:** `OrganizationMemberRepository` ganha
  `findRemovedByOrgAndUser` e `reactivate(memberId, role)`; `AcceptInviteService`
  reativa quando houver vínculo removido e marca o convite `USED` como hoje.
- **Turma:** `ClassroomRepository` ganha `findRemovedMember` e
  `reactivateMember(memberId, role)`; `JoinClassroomService` (papel `ALUNO`) e
  `AddClassroomMemberService` (papel do comando) reativam do mesmo jeito.
- **Sessão:** voltar é entrar numa organização; o aceite no front já reemite o
  token (#77). Nenhum evento novo de sessão obsoleta.

## Scope

### In

- Reativação no aceite de convite da organização + testes unitários e IT
  (remover → reconvidar → aceitar → membro ativo com o papel novo).
- Reativação ao entrar por código e ao adicionar membro na turma + testes
  unitários e IT.
- Validação na aplicação dos dois caminhos.

### Out

- Mudar `uq_member`/`uq_classroom_member` ou o soft delete.
- Outros vínculos com restrição única e soft delete (ex.: `task_submissions`).
- Vínculos de turma de quem foi removido da organização.
- Login/redirecionamento do convite (#198) e regras de reenvio/cancelamento (#77).

## Subtasks

- [ ] BE organização: aceite de convite reativa o vínculo removido (repositório + service) + testes unitários e IT
- [ ] BE turma: entrar por código e adicionar membro reativam o vínculo removido + testes unitários e IT
- [ ] Validar na aplicação: membro removido volta por convite; aluno removido volta à turma pelo código
