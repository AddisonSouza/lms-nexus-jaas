# Listagem de turmas não exibe turmas existentes

## Objetivo

Corrigir a list-view de turmas, que não está trazendo as turmas já cadastradas
na organização — a tela aparece vazia mesmo havendo turmas existentes.

## Comportamento esperado

Ao abrir a listagem de turmas, as turmas da organização do usuário logado devem
ser carregadas e exibidas, respeitando o papel de quem acessa e o
`organization_id` do JWT. O estado vazio só deve aparecer quando realmente não
houver turma.

## Fora de escopo

- Redesenhar a listagem.
- Adicionar filtros, busca, paginação ou novas colunas.

A correção se limita a fazer os dados existentes aparecerem.
