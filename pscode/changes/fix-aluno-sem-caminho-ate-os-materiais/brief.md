# fix: aluno sem caminho até os materiais

**Card:** #273 · **Severidade:** alto · **RF-09, RF-10** · origem: bateria E2E 15/09/2026

## Objetivo

Dar ao aluno um caminho navegável até os materiais das disciplinas das suas
turmas. Hoje a sidebar não tem "Disciplinas" para o papel ALUNO e `/curriculum`
responde 403 — exibido como mensagem de sessão expirada/conexão.

## Comportamento esperado

- O aluno chega às disciplinas das suas turmas a partir da UI, sem digitar URL.
- A página da disciplina (`/curriculum/:subjectId`) abre para o aluno em modo
  leitura: tópicos e materiais, sem ações de gestão.
- O aluno só enxerga disciplinas vinculadas às turmas de que é membro.
- 403 em listagens mostra mensagem de permissão, não de conexão/sessão.

## Fora do escopo

- Gestão de conteúdo pelo aluno (criar/editar/excluir tópico ou material).
- Vínculo disciplina↔turma pela UI (card separado).
- Refatoração do interceptor do axios / toasts globais.
