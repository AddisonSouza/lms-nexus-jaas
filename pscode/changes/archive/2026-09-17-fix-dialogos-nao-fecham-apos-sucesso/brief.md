# Diálogos não fecham após sucesso (avaliar, adicionar membro)

**Severidade:** alto · RF-13, RF-07 · origem: bateria E2E 15/09/2026

## Objective
Fechar o diálogo e atualizar a lista após avaliar submissão (PATCH 200) e
adicionar membro à turma (POST 201). Hoje o diálogo fica aberto, a lista só
atualiza após reload e um segundo "Salvar" gera 422 silencioso.

## Expected behavior
- Após sucesso, o diálogo fecha e a lista de submissões / membros é atualizada.
- Botão de submit desabilitado enquanto a requisição está pendente.

## Out of scope
- Diálogos que já fecham corretamente (turma, disciplina, tópico).
