# [feat] acessos de professor para admin e gestor — Delta

## Added
- RN-09 (`DECISIONS.md`): hierarquia de ensino `ADMIN_ORG > GESTOR > PROFESSOR`.
  ADMIN_ORG e GESTOR também podem lecionar, sempre limitados pelo vínculo com a
  disciplina, turma ou tarefa. O papel continua único por membro.
- `subject-teacher-assignment`: cenário de vínculo de GESTOR/ADMIN_ORG como
  professor da disciplina (201).
- `route-authorization`: cenário do GESTOR vendo "Tarefas" no menu.
- Painel de professor da disciplina não é exibido (sem erro) quando a API
  responde 403 — gestor/admin que não leciona naquela disciplina.

## Changed
- Vínculo de professor: só PROFESSOR → PROFESSOR, GESTOR ou ADMIN_ORG; apenas
  ALUNO recebe 422 `MEMBER_NOT_A_PROFESSOR`.
- Endpoints de tarefas, correção de entregas, avisos e painel da disciplina:
  `PROFESSOR` → `PROFESSOR`, `GESTOR`, `ADMIN_ORG`. Criador da tarefa, autor do
  aviso e professor da turma/disciplina continuam obrigatórios.
- Web: menu "Tarefas", botão "Novo Aviso" e painel da disciplina usam
  `canTeach(role)` em vez de `role === 'PROFESSOR'`.
- `route-authorization`: `/assessment/tasks` passa de `PROFESSOR` para
  `PROFESSOR`, `GESTOR`, `ADMIN_ORG`.
