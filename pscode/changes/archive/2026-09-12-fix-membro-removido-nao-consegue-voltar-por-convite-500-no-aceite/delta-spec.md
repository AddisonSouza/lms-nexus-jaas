# Membro removido volta à organização e à turma — Delta

## Changed

- **Aceitar convite reativa o vínculo removido.** Remover um membro é soft
  delete (`organization_members.deleted_at`), e `uq_member (organization_id,
  user_id)` ignora o `deleted_at`. O `AcceptInviteService` só procurava vínculo
  ativo, inseria outra linha e respondia 500. Agora, havendo vínculo removido,
  ele é reativado: `deleted_at = NULL`, papel do convite, `joined_at` da volta.
- **Entrar na turma pelo código reativa o vínculo removido**
  (`JoinClassroomService`, papel `ALUNO`, 201 como uma entrada nova), e
  **adicionar membro** (`AddClassroomMemberService`) reativa com o papel pedido
  — mesma causa, com `uq_classroom_member (classroom_id, user_id)`.

## Added

- Portas: `OrganizationMemberRepository.findRemovedByOrgAndUser`/`reactivate` e
  `ClassroomRepository.findRemovedMember`/`reactivateMember`. A reativação é um
  `UPDATE` explícito, porque `joined_at` é `updatable = false` nas entidades.
- Testes: casos de reativação em `AcceptInviteServiceTest`,
  `JoinClassroomServiceTest` e `AddClassroomMemberServiceTest`; ITs em
  `InvitationResourceIT` (removido → convite → aceite → uma linha ativa, papel
  novo) e `JoinClassroomIsolationIT` (entra → removido → entra de novo → 201).

## Unchanged

- `uq_member`, `uq_classroom_member` e o soft delete (DB-05). Sem migração.
- Nenhum evento novo de sessão: o aceite no front já reemite o token.

## Reported, not fixed

- **#210** — sair pela tela de aceite leva a `/login?invite=<token>`, e qualquer
  conta que entra ali cai no convite de outra pessoa (regressão do #198). Achado
  no teste manual deste card.
- A API grava datas com o horário local da JVM (-03) e o MySQL de dev está em
  UTC; um IT precisou de folga de um dia por isso.
- `getResultStream().findFirst()` deixa ResultSet aberto
  (`JDBC resources leaked`) em vários repositórios — padrão pré-existente, também
  usado nos métodos novos.
