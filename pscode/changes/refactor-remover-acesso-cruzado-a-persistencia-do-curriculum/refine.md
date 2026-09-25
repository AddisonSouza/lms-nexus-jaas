# [refactor] remover acesso cruzado à persistência do curriculum

## Summary
Três módulos leem direto as tabelas internas de disciplinas. A mudança faz
esses módulos pedirem os dados ao módulo `curriculum`, que é o dono das tabelas,
sem mudar nada do que o usuário vê.

## Technical detail
- Criar `curriculum/domain/port/in/SubjectDirectoryPort`, implementado em
  `curriculum/application`, que usa o `SubjectRepository` e o
  `OrganizationMemberQueryPort`. Operações:
  - turmas de uma disciplina;
  - disciplinas de uma ou mais turmas;
  - userIds dos professores de uma disciplina;
  - se um usuário leciona uma disciplina (org-scoped);
  - se a disciplina existe (org-scoped, ignora soft delete);
  - nomes das disciplinas por id.
- Hoje a tradução só vai de member para user (`findUserIdsByMemberIds`).
  Adicionar ao port a tradução inversa, de userId para memberId.
- Nos adapters consumidores, trocar a subquery `IN (SELECT … FROM *JpaEntity)`
  por `IN :ids` com os ids vindos do port. Com lista vazia, retornar
  vazio ou 0 sem consultar o banco.
- Os JOINs em `SubjectJpaEntity` usados para buscar o nome da disciplina viram
  uma busca de nomes por id, montada em memória.
- Rede de segurança: os ITs que já existem (`*DashboardQueryPortImplIT`,
  `*DashboardResourceIT` e os de communication/assessment) precisam continuar verdes.

## Scope
### In
- `assessment/SubjectQueryAdapter`
- `communication/SubjectQueryPortImpl`
- `reporting`: `Student`, `Gestor`, `Professor` e `TaskMetrics` QueryPortImpl
- As entidades `SubjectJpaEntity`, `SubjectClassroomJpaEntity` e `SubjectTeacherJpaEntity`
### Out
- Schema, migrations e views.
- Acesso cruzado a entidades de `classroom`, `assessment`, `identity` e `organization`
  (há 16 adapters com esse padrão; vale um card próprio).
- Mudanças no cálculo dos dashboards.

## Subtasks
- [x] Criar `SubjectDirectoryPort` no curriculum, com implementação e IT (Testcontainers)
- [x] Migrar `assessment/SubjectQueryAdapter` para o port
- [x] Migrar `communication/SubjectQueryPortImpl` para o port
- [x] Migrar `reporting` Student e TaskMetrics QueryPortImpl para o port
- [x] Migrar `reporting` Gestor e Professor QueryPortImpl para o port
- [x] Garantir por grep que nenhuma entidade JPA do curriculum é citada fora do módulo e rodar a suíte completa
