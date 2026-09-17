# feat: vínculo disciplina↔turma e professor na UI — Delta

## Added
- **Seção "Turmas e Professores" em `/curriculum/:subjectId`**, visível só para
  `ADMIN_ORG` e `GESTOR`. Lista as turmas e os professores da disciplina **pelo
  nome** — `GET /subjects/{id}` devolve só ids, resolvidos no cliente cruzando
  com as listas da organização. Vazio explica a consequência: sem turma o aluno
  não vê a disciplina, sem professor ninguém cria tarefas nela.
- **Selo "Arquivada"** na turma vinculada que foi arquivada depois. O vínculo
  continua valendo; só o novo vínculo é recusado.
- **Clients e hooks próprios do `curriculum`** (FE-11 — feature não importa
  client de outra): `org-classroom-api` (`listOrgClassrooms`) e `org-member-api`
  (`listOrgMembers`), com Zod mínimo, mais `useOrgClassrooms` e
  `useTeacherCandidates`. A organização vem do `authStore`, nunca do request.
- **`MEMBER_NOT_A_PROFESSOR`** no tradutor de erros (`api-error`).
- Testes: `ListOrganizationMembersResourceIT` cobrindo `GESTOR`; 11 testes da
  seção no `SubjectDetailPage`; 5 por diálogo; 4 dos hooks novos.

## Changed
- **`GET /organizations/{id}/members`** aceita `GESTOR` além de `ADMIN_ORG` —
  o gestor atribui professores a disciplinas e precisa da lista. `PATCH` e
  `DELETE` de membro seguem só com `ADMIN_ORG`, e a rota `/members` da UI
  continua `ADMIN_ORG`. A organização continua vindo do claim `org`.
- **`LinkClassroomDialog`**: campo de UUID → seleção em lista, com só as turmas
  `ACTIVE` ainda não vinculadas. Recusa da API (422 `CLASSROOM_ARCHIVED`)
  aparece no próprio diálogo, que segue aberto.
- **`AssignTeacherDialog`**: campo de UUID → seleção em lista dos membros que a
  API aceita como professor (`PROFESSOR`, `GESTOR`, `ADMIN_ORG`) e ainda não
  atribuídos, com o papel ao lado do nome. Envia o **`memberId`** (id do vínculo
  em `organization_members`), não o `userId`.
- **Desvincular turma e remover professor** passam por `ConfirmDialog`, com a
  recusa da API exibida na confirmação.
- **`API_CONTRACT.md` (RF-09)**: payload de professor corrigido de
  `{userId, classroomId}` para `{memberId}`, os dois `DELETE` documentados,
  além dos códigos `200` (idempotente), `404` e `422` de cada rota.

## Removed
- Nada.

## Conhecido, fora deste card
- A seção não aparece para `PROFESSOR` nem `ALUNO` — decisão de escopo, não
  limitação técnica. O professor segue gerenciando só o conteúdo.
