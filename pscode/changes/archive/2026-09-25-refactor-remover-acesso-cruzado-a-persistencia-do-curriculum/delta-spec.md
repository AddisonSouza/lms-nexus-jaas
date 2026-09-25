# [refactor] remover acesso cruzado à persistência do curriculum — Delta

## Added
- `SubjectDirectoryPort` (`curriculum/domain/port/in`): turmas de uma disciplina,
  disciplinas de várias turmas, professores (userIds ativos), `isTeacherOfSubject`
  (com e sem organização), `existsSubject` e nomes por id (inclui excluídas).
- Cenários "Avaliação sem nota" nas specs `gestor-dashboard`,
  `professor-dashboard` e `student-dashboard`.

## Changed
- `assessment`, `communication` e `reporting` leem vínculos de disciplina pelo
  port, não mais pelas entidades JPA do curriculum. Respostas inalteradas.
- Avaliação só com feedback (`grade` nulo): antes derrubava os dashboards
  (NPE → 500 no gestor e nas médias; `[null]` na distribuição; Zod rejeitava no
  aluno) → agora fica fora de médias e da distribuição, e aparece nas últimas
  notas do aluno como "—" com o feedback.
