# Mensagem interna do Zod ao adicionar membro sem papel — Delta

## Changed

- **Adicionar membro à turma sem escolher o papel.** O diálogo mostrava
  `Invalid enum value. Expected 'PROFESSOR' | 'ALUNO'` — texto interno da
  biblioteca de validação. Agora mostra **"Selecione o papel"**.
  Causa: `z.enum([...], { required_error })` só cobre valor **ausente**, e o
  `<select>` envia `""`, que cai em `invalid_enum_value`. `errorMap` cobre
  ausente, vazio e inválido — mesma solução aplicada ao `inviteMemberSchema` na
  #139.
- **Rótulos do `AddMemberDialog` ligados aos campos.** Os `<label>` não tinham
  `htmlFor` nem os campos `id`, então clicar no rótulo não focava o campo e o
  campo não tinha nome acessível. As mensagens de erro passam a ser `role="alert"`
  e os campos carregam `aria-invalid`, no padrão do `InviteMemberDialog`.

## Added

- **Teste do `AddMemberDialog`** (`AddMemberDialog.test.tsx`), o primeiro do
  componente: submeter sem papel (afirmando que o texto do Zod **não** aparece),
  sem `userId`, o caminho feliz, limpar ao cancelar e o envio bloqueado enquanto
  pendente. Verificado que o primeiro teste falha contra o schema antigo.

## Notes

É a única ocorrência do defeito no front. `classroomSchema.status` e
`contentSchema.contentType` também são `z.enum`, mas seus selects não têm opção
vazia, então nunca enviam `""` — ficaram de fora, como decidido no refinamento,
junto com a UX de colar UUID na mão para adicionar membro.
