# fix: aluno sem caminho até os materiais — Delta

## Added
- **`ClassroomQueryPort.findClassroomIdsByUser(userId, orgId)`** no curriculum:
  as turmas de que o usuário é membro, numa consulta só, para filtrar a
  listagem sem checar matrícula disciplina por disciplina.
- **Checagem de matrícula em `GET /subjects/{id}/topics`**. A rota já aceitava
  `ALUNO` mas não verificava nada: qualquer aluno da organização lia os tópicos
  de qualquer disciplina, inclusive de turmas de que não participa. `/contents`
  já barrava; `/topics` não. Agora usa a mesma regra
  (`isMemberOfAnyClassroom` → 403 `CONTENT_ACCESS_DENIED`).
- **Estado de 403 no `ListErrorState`**: ícone de cadeado, texto de permissão e
  **sem** botão "Tentar de novo" — tentar de novo não resolve permissão. Turmas,
  disciplinas, membros e tarefas do aluno herdam.
- **Nome e código da disciplina** no cabeçalho da `SubjectDetailPage`, via
  `useSubject(id)`, com fallback para o título genérico enquanto carrega.
- Primeiro teste da `Sidebar`, cobrindo o que o aluno passa a ver e o que
  segue escondido (`Tarefas`, `Membros`).

## Changed
- **`GET /subjects`** aceita `ALUNO` e devolve só as disciplinas vinculadas às
  turmas de que ele é membro. Disciplina sem turma vinculada não alcança nenhum
  aluno. Papéis de gestão continuam vendo todas as da organização.
- **`GET /subjects/{id}`** aceita `ALUNO` com a mesma checagem de matrícula;
  403 `CONTENT_ACCESS_DENIED` fora dela.
- **`ListSubjectsUseCase`, `GetSubjectUseCase` e `ListTopicsUseCase`** passam a
  receber `requestingUserId` e `requestingUserRole`, seguindo o formato que
  `ListSubjectContentsUseCase` já usava.
- **Sidebar**: "Disciplinas" perde a condição `canTeach(role)` — a rota
  `/curriculum` nunca teve guarda de papel, e a listagem chega filtrada da API.

## Removed
- Nada.

## Conhecido, fora deste card
- Num 403 de disciplina alheia, a `SubjectDetailPage` desenha "Nenhum tópico
  cadastrado ainda" — o aluno vê a disciplina como vazia em vez de sem
  permissão. Mesmo sintoma que este card corrigiu nas listagens, numa tela que
  não usa `ListErrorState`. Comportamento pré-existente.
