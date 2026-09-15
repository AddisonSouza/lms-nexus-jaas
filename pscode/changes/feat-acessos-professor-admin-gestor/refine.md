# [feat] acessos de professor para admin e gestor

## Summary
Gestores e administradores da organização passam a poder também dar aula:
ser vinculados a disciplinas, criar e corrigir tarefas e publicar avisos, sem
precisar de outra conta — como um coordenador de curso que também leciona.

## Technical detail
- Viável sem migration: hierarquia de papéis `ADMIN_ORG > GESTOR > PROFESSOR`.
  O papel continua único por membro (`organization_members.role`); o acesso de
  professor fica limitado pelos vínculos que já existem (`subject_teachers`,
  `classroom_members.role='PROFESSOR'` e o autor da tarefa).
- Bloqueio atual: `OrganizationMemberQueryPortImpl.java:30` só aceita
  `m.role = 'PROFESSOR'`, então `AssignTeacherToSubjectService` recusa um gestor
  com `MEMBER_NOT_A_PROFESSOR`.
- Endpoints que hoje aceitam só `PROFESSOR` e passam a aceitar os três papéis:
  `TaskResource` (:56, :84, :130, :196), `SubmissionResource:32`,
  `AnnouncementResource` (:33, :62), `ClassroomAnnouncementResource:35` e
  `ProfessorDashboardResource:24`.
- O JWT não muda (`groups` continua com um único papel), para não afetar
  `authStore` (`groups[0]`).
- Web: um helper `canTeach(role)` substitui os `role === 'PROFESSOR'` em
  `Sidebar:46`, `AnnouncementFeed:20` e `SubjectDetailRoute:12`, além do texto
  de exemplo em `AssignTeacherDialog`.

## Scope
### In
- Vincular um GESTOR ou ADMIN_ORG como professor de uma disciplina.
- Tarefas, correções, avisos e o painel de professor para esses papéis, limitados
  pelos vínculos acima.
- Menu, feed de avisos e detalhe da disciplina no front.
- Atualizar as specs `route-authorization` e `subject-teacher-assignment`, e
  registrar a regra em `DECISIONS.md`.
### Out
- Vários papéis por membro, flag "leciona" ou mudança no JWT.
- Seletor para alternar a visão da home (a home continua a de gestão).
- Qualquer mudança nos acessos de ALUNO ou entre organizações.

## Subtasks
- [x] API: permitir vincular GESTOR/ADMIN_ORG como professor de disciplina (query port + teste de `AssignTeacherToSubjectService`)
- [ ] API: liberar para GESTOR/ADMIN_ORG os endpoints de tarefas, entregas, avisos e painel de professor (+ testes de integração)
- [ ] Web: criar o helper `canTeach` e aplicá-lo no Sidebar, no AnnouncementFeed e no SubjectDetailRoute
- [ ] Docs: registrar a hierarquia em DECISIONS.md e atualizar as specs `route-authorization` e `subject-teacher-assignment`
