# Código fixo de turma visível na listagem

## Summary

O código de acesso da turma passa a ser fixo: gerado uma única vez na criação e
imutável para sempre. A opção de "regerar código" some da tela e da API. Em
troca, o código passa a aparecer direto na listagem de turmas — com botão de
copiar — para quem administra ou leciona (`ADMIN_ORG`, `GESTOR`, `PROFESSOR`).
O aluno continua entrando pelo código, mas não vê o código das suas turmas.

## Technical detail

- **Remoção da regeneração (back-end):** apaga `POST /classrooms/{id}/invite-code/regenerate`
  em `ClassroomResource`, o `RegenerateInviteCodeUseCase`, o
  `RegenerateInviteCodeService` e o `RegenerateInviteCodeServiceTest`. Nenhum
  outro caminho reescreve `inviteCode`: `UpdateClassroomService` já preserva o
  valor via `toBuilder()`, e a geração fica só em `CreateClassroomService`.
  `Classroom.inviteCode` vira `final`, documentando a imutabilidade no domínio.
- **Visibilidade por papel:** hoje `ListClassroomsService` devolve `inviteCode`
  para todos os papéis (inclusive `ALUNO`) e `GetClassroomService` esconde o
  código de `PROFESSOR` também. A regra passa a ser única — `ADMIN_ORG`,
  `GESTOR` e `PROFESSOR` recebem o código; `ALUNO` recebe `inviteCode: null` —
  aplicada nos dois serviços a partir do `requesterRole` que ambos já recebem.
- **Listagem (front-end):** nova coluna "Código" em `ClassroomListPage`, em
  fonte mono com ícone `Copy` (mesmo padrão do `ClassroomDetailPage`). A coluna
  só é renderizada para os papéis que recebem o código.
- **Detalhe (front-end):** sai o botão `RefreshCw`; some o hook
  `useRegenerateInviteCode` e a função `regenerateInviteCode` do
  `classroom-api.ts`. O card "Código de Convite" e o botão copiar permanecem.
- **Contrato/specs:** `API_CONTRACT.md` (RF-08), `docs/requirements/RF.md`
  (RF-08) e `pscode/specs/classroom-join-by-code/spec.md` perdem o requisito
  "Regenerate invite code"; `pscode/specs/classroom-management/spec.md` passa a
  registrar a visibilidade do código incluindo `PROFESSOR`.
- Sem migration: nenhuma mudança de schema; turmas existentes mantêm o código.

## Scope

### In

- Remover o endpoint, o use case, o service e os testes de regeneração.
- Tornar `inviteCode` imutável no domínio.
- Unificar a regra de visibilidade do código em list + detalhe.
- Exibir o código na listagem de turmas, com copiar.
- Remover a ação de regenerar do front-end.
- Atualizar `API_CONTRACT.md`, `RF.md` e as specs afetadas.

### Out

- Formato e algoritmo do código (6 chars alfanuméricos, `InviteCode.generate()`).
- Fluxo de ingresso do aluno (`POST /classrooms/join`) e `JoinClassroomForm`.
- Códigos já emitidos — nada é regravado.
- A resposta de `POST /classrooms/join`, que devolve o código ao usuário que
  acabou de digitá-lo.
- Migrar o mapeamento manual `toResponse` para MapStruct.


## Subtasks

- [x] Remover a regeneração no back-end: endpoint, use case, service e testes (#79)
- [x] Tornar `Classroom.inviteCode` imutável (`final`) no domínio (#80)
- [x] Unificar visibilidade do `inviteCode` (ADMIN_ORG, GESTOR, PROFESSOR) em `ListClassroomsService` e `GetClassroomService`, com testes (#81)
- [x] Remover `useRegenerateInviteCode`, `regenerateInviteCode` e o botão de regenerar do `ClassroomDetailPage` (#82)
- [x] Adicionar a coluna "Código" com botão copiar em `ClassroomListPage`, visível só para os papéis que recebem o código (#83)
- [x] Atualizar `API_CONTRACT.md`, `docs/requirements/RF.md` e as specs `classroom-join-by-code` / `classroom-management` (#84)
