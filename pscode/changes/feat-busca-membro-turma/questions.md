# Grill Me — feat: busca por nome ou e-mail ao adicionar membro à turma

## 1. A busca filtra onde?

**Resposta:** no **backend**, com **paginação**.

`GET /organizations/{id}/members` passa a aceitar busca por nome/e-mail e
paginação. Consequência: a resposta deixa de ser um array cru e vira o envelope
paginado, o que quebra os dois consumidores atuais do endpoint.

## 2. Que primitivo de UI usar para a busca?

**Resposta:** **Combobox sobre `@base-ui/react`**.

Criar `apps/web/src/components/ui/combobox.tsx` envolvendo `@base-ui/react/combobox`,
no mesmo padrão do `popover.tsx` existente. A dependência já está instalada e o
design system não tem nenhum combobox/autocomplete hoje.

## 3. O `AssignTeacherDialog` entra neste card?

**Resposta:** **sim, os dois no mesmo card**.

O diálogo de atribuir professor a disciplina tem o mesmo problema (`<select>`
nativo, sem busca) e consome o mesmo endpoint. Adota o Combobox junto.

## 4. Como tratar quem já está na turma nos resultados?

**Resposta:** **mostrar marcado como "já na turma"**, desabilitado.

Evita a confusão de procurar alguém e não encontrar, e é coerente com a
idempotência que a API já garante.

## Em aberto

- [ ] Nenhuma pendência bloqueante.
