# Perguntas

- [x] **Incluir teste do `AddMemberDialog`?**
      → Sim. O diálogo não tem nenhum teste hoje; sem ele o defeito volta.
- [x] **Corrigir os `<label>` sem `htmlFor` junto?**
      → Sim. Liga rótulo e campo por `id`, como no `InviteMemberDialog` da #139;
      habilita a busca por label no teste e melhora a acessibilidade.

## Premissas (verificadas no código, não questionadas)

- `addMemberSchema.role` é o **único** `z.enum` do front ligado a um `<select>`
  com opção vazia. `classroomSchema.status` e `contentSchema.contentType` não têm
  opção vazia, então não vazam a mensagem interna — ficam de fora.
