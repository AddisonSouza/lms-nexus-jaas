## Why

RF-07 introduz o módulo `classroom` — responsável pela criação e gestão de turmas dentro de uma organização. Sem turmas, os demais módulos do MVP (currículo, avaliação, comunicação) não têm contexto para operar.

## What Changes

- Novo módulo `classroom` no backend (Quarkus): domain, use cases, REST, Flyway migrations
- Nova feature `classroom` no frontend (React): listagem, criação, edição, exclusão e gestão de membros
- `ADMIN_ORG` e `GESTOR` podem criar, editar e excluir turmas (soft delete)
- Todos os membros da organização podem listar turmas às quais pertencem
- Turma criada com código de convite aleatório de 6 caracteres (base para RF-08)
- `classroom_members`: vínculo entre turma e membros com papel `PROFESSOR` ou `ALUNO`

## Capabilities

### New Capabilities

- `classroom-management`: CRUD completo de turmas (nome, descrição, período letivo, status) com RBAC por papel; geração de código de convite na criação
- `classroom-members`: vinculação e remoção de membros (PROFESSOR/ALUNO) de uma turma; listagem de membros por turma

### Modified Capabilities

_(nenhuma — módulo novo)_

## Impact

- **Backend:** novo módulo `br.edu.lms.module.classroom`; Flyway migrations V007 e V008; endpoints `/classrooms`
- **Frontend:** nova feature `features/classroom`; novas rotas protegidas por papel
- **Sem impacto em:** módulos existentes (identity, organization); sem breaking changes nas APIs existentes

## Non-goals

- Ingresso via código público (RF-08 — change separada)
- Vínculo turma-disciplina (RF-09)
- Dashboards e relatórios por turma (RF-17 a RF-20)
- Regeneração de código de convite (RF-08)
