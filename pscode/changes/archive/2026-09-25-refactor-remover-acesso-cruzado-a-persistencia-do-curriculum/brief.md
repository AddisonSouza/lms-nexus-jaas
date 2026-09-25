# Remover acesso cruzado à persistência do curriculum

## Objetivo
Hoje `reporting`, `assessment` e `communication` leem as entidades JPA do
`curriculum` escrevendo o nome completo delas dentro de JPQL. Isso viola as regras
MOD-03 e MOD-05. A ideia é trocar esse acesso por um port de entrada exposto pelo
`curriculum`.

## Comportamento esperado
- O que os endpoints retornam não muda.
- Nenhum módulo fora do `curriculum` referencia entidades JPA dele.

## Fora do escopo
- Mudar o schema ou migrations.
- Mudar o que os dashboards calculam.
- Acessos cruzados que não envolvam entidades do `curriculum`.
