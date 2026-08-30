# Grill Me — listagem de turmas vazia

- [x] **Em qual papel a listagem aparece vazia?** → **ADMIN_ORG / GESTOR**.
  Isola o caminho `findAllByOrganization(orgId)`; o caminho `findAllByMember`
  (PROFESSOR/ALUNO) não é o relatado.
- [x] **Ainda reproduz depois do fix multi-organização (#120, 26/08)?** → Não
  testado desde então. Decisão: subir o ambiente e reproduzir antes de definir
  a correção, para não consertar algo já resolvido.
- [x] **Tratar estado de erro na tela entra no escopo?** → **Sim.** A tela hoje
  ignora `isError`, então qualquer falha vira "tabela vazia" silenciosa.
- [x] **Se a causa raiz for compartilhada com a #75 (disciplinas)?** → **Cobrir
  as duas juntas** nesta change; a #75 é fechada junto.

## Abertas

- [ ] Confirmar a causa raiz na reprodução: claim `org` do JWT, dados ausentes
      no banco de dev, ou falha de parse Zod na resposta.
