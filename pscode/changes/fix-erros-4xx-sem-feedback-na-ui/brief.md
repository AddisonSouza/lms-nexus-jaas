# [fix] erros 4xx sem feedback na UI

## Objetivo
Mostrar ao usuário o erro devolvido pela API nos formulários que hoje falham em
silêncio ou com texto genérico. Origem: bateria E2E de 15/09/2026.

## Comportamento esperado
- Casos observados: publicar aviso como admin (403), excluir disciplina como
  gestor (403), upload `.exe` em conteúdo (422), reset de senha fraca (422 vira
  "Erro ao redefinir senha"), aceitar convite de outro e-mail (403 vira "Tente
  novamente"), nota acima do máximo (só bloqueio nativo, sem texto).
- Mensagem derivada de `error` (código) ou `errors` (lista de validação) da
  resposta, por um helper compartilhado em `lib/`.
- `ConfirmDialog` exibe o erro e permanece aberto quando a ação falha.

## Fora do escopo
- Toasts globais; refatoração do interceptor Axios.
- Esconder ações proibidas ao papel (#275); regras de autorização no backend.
