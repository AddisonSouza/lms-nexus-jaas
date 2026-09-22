# feat: busca por nome ou e-mail ao adicionar membro à turma

## Summary

Hoje, para colocar alguém numa turma, é preciso colar o UUID da pessoa — um dado
que ninguém conhece de cabeça e que a tela não ajuda a descobrir. O mesmo vale
para atribuir um professor a uma disciplina, que usa uma lista solta sem busca.
Esta change troca as duas telas por um campo de busca por nome ou e-mail entre os
membros da organização, e faz a API suportar essa busca de forma paginada.

## Technical detail

- **Busca e paginação na API.** `GET /organizations/{id}/members` passa a aceitar
  `?search=&page=&size=`, seguindo o padrão já documentado em
  `API_CONTRACT.md:955-963` (`{ content, totalElements, totalPages, number, size }`).
  Nenhum endpoint do projeto implementa esse padrão ainda — este é o primeiro.
- **A busca precisa atravessar módulos.** Nome e e-mail não estão em
  `organization_members`: hoje vêm de uma segunda consulta
  (`UserDirectoryPort.findProfilesByIds`) e a ordenação por nome é feita em
  memória (`ListOrganizationMembersService:21-23,45`). Para filtrar e paginar em
  SQL, o join passa a acontecer no repositório — há precedente no mesmo arquivo
  (`OrganizationMemberRepositoryImpl.java:89`, join cross-módulo por FQN).
- **Mudança quebra os consumidores atuais.** `organization-api.ts:58` e
  `curriculum/api/org-member-api.ts:20` fazem `z.array(...).parse(data)`; ambos
  precisam ler o envelope. Afeta `OrganizationMembersPage` e `useTeacherCandidates`.
- **O design system não tem combobox.** Não existe Command, Combobox,
  Autocomplete nem debounce em `apps/web/src`. O `@base-ui/react` (já instalado)
  expõe `./combobox`; criamos o wrapper local no padrão do `popover.tsx`.
- **Dois ids em jogo.** `POST /classrooms/{id}/members` espera `userId`;
  `POST /subjects/{id}/teachers` espera `memberId` (id do vínculo). O DTO do
  listing já devolve os dois, então o combobox expõe o item inteiro e cada tela
  escolhe o campo — sem unificar contratos aqui.
- **"Já na turma".** O `AddMemberDialog` cruza os resultados com os membros da
  turma (já em cache por `classroomKeys.members`) e desabilita quem já está lá.

## Scope

### In
- BE: `?search=&page=&size=` em `GET /organizations/{id}/members`, com filtro e
  paginação em SQL e resposta no envelope padrão.
- BE: porta, use case, repositório e `API_CONTRACT.md` atualizados, com testes.
- FE: `components/ui/combobox.tsx` sobre `@base-ui/react` e hook de debounce.
- FE: `AddMemberDialog` com busca por nome/e-mail e marcação "já na turma".
- FE: `AssignTeacherDialog` adotando o mesmo combobox.
- FE: `OrganizationMembersPage` e `useTeacherCandidates` adequados ao envelope.

### Out
- Convidar pessoas de fora da organização; alterar papéis; adição em lote.
- Parâmetro `sort` configurável — a ordenação segue fixa por nome.
- Paginação nos demais endpoints de listagem do projeto.
- Unificar `userId` vs `memberId` entre os dois contratos.

## Subtasks
- [x] BE: filtro por nome/e-mail e paginação em SQL no `OrganizationMemberRepositoryImpl`, com teste
- [x] BE: porta + `ListOrganizationMembersService` devolvendo página, com teste unitário
- [x] BE: `@QueryParam` no `OrganizationResource` e envelope na resposta, com `ListOrganizationMembersResourceIT` atualizado
- [x] Atualizar `API_CONTRACT.md` do endpoint e a spec `member-invitations`
- [ ] FE: `components/ui/combobox.tsx` sobre `@base-ui/react`, com teste
- [ ] FE: hook `useDebouncedValue`, com teste
- [ ] FE: adequar `organization-api`, `org-member-api` e seus hooks ao envelope, com testes
- [ ] FE: `AddMemberDialog` com busca e marcação "já na turma", com teste
- [ ] FE: `AssignTeacherDialog` adotando o combobox, com teste
- [ ] Validar os dois fluxos no navegador; rodar lint, type-check e as duas suítes
