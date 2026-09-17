# Vínculo disciplina↔turma e professor na UI

## Summary
Na página da disciplina, administradores e gestores passam a ver quais turmas e
professores estão ligados a ela e podem vincular ou remover escolhendo em listas,
sem digitar códigos. Sem isso, professores não criam tarefas e alunos não veem materiais.

## Technical detail
- Endpoints já existem (`POST/DELETE /subjects/{id}/classrooms`, `POST/DELETE /subjects/{id}/teachers`); hooks `useLink/Unlink/Assign/RemoveTeacher` também — nenhum usado em página.
- `GET /subjects/{id}` só devolve `classroomIds`/`teacherMemberIds`; nomes resolvidos no cliente cruzando com as listas.
- `memberId` é o **`id` do vínculo** `organization_members`, não o `userId`.
- `GET /organizations/{id}/members` é `@RolesAllowed("ADMIN_ORG")` → incluir `GESTOR`.
- `GET /classrooms` já retorna todas as turmas da org para ADMIN_ORG/GESTOR (inclui arquivadas).
- FE-11: `curriculum` ganha clients próprios (Zod mínimo) para turmas e membros; `organizationId` via `authStore`.
- Seletor de turmas: só `ACTIVE` e ainda não vinculadas; turma vinculada arquivada aparece com selo "Arquivada".
- Seletor de professor: só PROFESSOR/GESTOR/ADMIN_ORG ainda não atribuídos.
- 422 (`CLASSROOM_ARCHIVED`, `MEMBER_NOT_A_PROFESSOR`/`NOT_IN_ORGANIZATION`) exibidos no diálogo; remoções com `ConfirmDialog`.

## Scope
### In
- Liberar listagem de membros para GESTOR (+ teste de integração).
- Seção "Turmas e Professores" em `/curriculum/:subjectId` só para ADMIN_ORG/GESTOR.
- Diálogos por lista (sem UUID) e exibição de erros da API.
- Atualizar `API_CONTRACT.md` (payload `memberId`, DELETEs, 422).
### Out
- Reordenação de tópicos; painel do professor.
- Visão da seção para PROFESSOR/ALUNO.
- Acesso de GESTOR à página de membros (rota continua ADMIN_ORG).

## Subtasks
- [ ] api: permitir GESTOR em `GET /organizations/{id}/members` + teste de integração
- [ ] web: clients e hooks em `curriculum` para turmas da org e candidatos a professor (Zod)
- [ ] web: `LinkClassroomDialog` com seleção de turmas ativas não vinculadas + erro 422 + testes
- [ ] web: `AssignTeacherDialog` com seleção de membros elegíveis não atribuídos + erro 422 + testes
- [ ] web: seção "Turmas e Professores" no `SubjectDetailPage` (listar, desvincular/remover com confirmação) + testes
- [ ] docs: atualizar RF-09 em `API_CONTRACT.md`
