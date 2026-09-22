# feat: busca por nome ou e-mail ao adicionar membro à turma

## Objetivo

Substituir a entrada por UUID no modal de adicionar membro a uma turma por uma
busca por nome ou e-mail entre as pessoas que já pertencem à organização.

## Comportamento esperado

- Na tela da turma, o usuário abre o modal de adicionar membro.
- Digita parte do nome ou do e-mail e vê os membros da organização que
  correspondem à busca.
- Seleciona a pessoa e confirma; ela passa a integrar a turma.
- Em nenhum momento é preciso conhecer, consultar ou colar um UUID.

## Fora de escopo

- Convidar pessoas de fora da organização.
- Alterar papéis ou permissões de quem é adicionado.
- Adição em lote (várias pessoas de uma vez).
