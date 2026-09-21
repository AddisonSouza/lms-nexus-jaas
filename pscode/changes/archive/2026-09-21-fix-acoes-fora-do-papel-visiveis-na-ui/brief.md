# fix: ações fora do papel visíveis na UI

**Severidade:** alto · origem: bateria E2E 15/09/2026

## Objetivo

Esconder da interface as ações que o papel do usuário logado não pode executar.
Hoje a UI oferece botões e blocos que sempre terminam em erro 403, o que passa a
impressão de que o sistema está quebrado.

## Comportamento esperado

- **"Novo Aviso"** — visível só para o professor vinculado. O mural de
  admin/gestor hoje nem carrega (403); é preciso decidir se admin/gestor devem
  poder ler o mural e, se sim, ajustar a API.
- **"Excluir" disciplina** — visível só para `ADMIN_ORG`.
- **"Entrar via código"** — visível só para `ALUNO`.
- **Bloco "Dashboard da Disciplina"** — oculto para quem não leciona a
  disciplina (hoje responde 403).

## Fora do escopo

- Mudar as regras de autorização do backend: a UI passa a refletir as regras que
  já existem, não a criá-las ou afrouxá-las.
