## Context

Módulo `assessment`. RF-12 (submissão) e RF-13 (avaliação) criaram os dados; RF-14 os expõe ao aluno. A tabela `task_submissions` já contém `grade`, `feedback` e `status` (SUBMITTED/EVALUATED). O endpoint atual `/tasks/published` retorna apenas dados da tarefa, sem a submissão do aluno. O aluno não tem como ver resultado.

## Goals / Non-Goals

**Goals:**
- Endpoint `GET /tasks/my-grades` retornando tarefa + submissão do aluno (ou null se não enviou)
- Endpoint `GET /submissions/{id}/feedback` para detalhe de feedback (apenas submissões EVALUATED)
- UI: `StudentTaskListPage` exibe status, nota e abre drawer com feedback completo

**Non-Goals:**
- Agregação de desempenho / médias (RF-20)
- Notificação ao aluno quando nota disponível (RF-16)
- Filtro/ordenação avançada

## Decisions

**D1 — Endpoint unificado em vez de duas chamadas separadas**  
`GET /tasks/my-grades` retorna `List<TaskWithGradeResponse>` combinando tarefa + submissão em um único round-trip. Alternativa (buscar tarefas depois submissões) causaria N+1 no frontend. Implementação: `SubmissionRepository.findByStudentAndOrganization` retorna mapa taskId→submission; loop sobre tarefas publicadas para montar o DTO.

**D2 — `findByStudentAndOrganization` no repositório**  
Consulta única `SELECT * FROM task_submissions WHERE student_id=? AND organization_id=? AND deleted_at IS NULL`. Retorna `List<TaskSubmission>`; o usecase monta um `Map<String, TaskSubmission>` para O(1) lookup por taskId. Não há FK direta entre submission e classroom, então o escopo é org-level, consistente com o restante do módulo.

**D3 — `GET /submissions/{id}/feedback` separado**  
Permite ao aluno acessar feedback de submissões específicas sem recarregar a lista. Retorna 403 se o aluno não é dono; retorna 404 se não encontrada; retorna 409 se ainda não avaliada (evitar expor nota prematura — critério de aceite do RF-14).

**D4 — Indicação de atraso no DTO**  
Campo booleano `lateSubmission` calculado no usecase: `submission.createdAt > task.deadline`. Evita lógica duplicada no frontend e centraliza a regra de negócio no backend.

**D5 — GradeFeedbackDrawer no frontend**  
Mesmo padrão do `SubmissionListDrawer` (professor). Drawer lateral exibe: título da tarefa, nota (badge), feedback textual, data de envio e indicador de atraso. Botão "Ver Nota" aparece somente quando `status === 'EVALUATED'`; botão "Editar Resposta" quando SUBMITTED e prazo não vencido.

## Risks / Trade-offs

- [Risco] Aluno com muitas tarefas → query pesada → Mitigation: índice em `(student_id, organization_id)` na tabela `task_submissions` (já deve existir pelo RF-12, verificar migration).
- [Trade-off] Endpoint org-scoped em vez de classroom-scoped (como especificado no RF): decisão consciente de consistência arquitetural — o módulo de tarefas não tem relação direta com classroom ainda.
