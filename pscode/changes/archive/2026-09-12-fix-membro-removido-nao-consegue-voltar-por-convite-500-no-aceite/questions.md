# Questions — #204

- [x] Como o membro removido volta? → **Reativar o vínculo removido**: limpar
  `deleted_at` e aplicar o papel do convite. `uq_member` fica como está, sem
  migração. (Mudar a restrição exigiria coluna gerada — no MySQL, `NULL` não
  conta como duplicado — e apagar de verdade contraria o soft delete, DB-05.)
- [x] O que a coluna "Ingresso" mostra na volta? → **A data da volta**:
  `joined_at` recebe a data do aceite novo.
- [x] Turma entra no card? → **Sim.** Mesma causa pelo código: entrar por código
  (`JoinClassroomService`) e adicionar membro (`AddClassroomMemberService`) só
  procuram vínculo ativo e inserem outro, esbarrando em
  `uq_classroom_member (classroom_id, user_id)`. Não reproduzido rodando.
