# Perguntas

- [x] **Alterar o papel de um membro entra nesta entrega?**
      → Sim. O RF-06 pede e não existe endpoint; entra junto (`PATCH`).
- [x] **Onde vive a interface?**
      → Rota própria `/organizations/:id/members` + item "Membros" na sidebar,
      seguindo o padrão de Turmas/Disciplinas.
- [x] **Mostra convites pendentes?**
      → Não. Só membros. Nenhum endpoint de listagem de convites nesta entrega.
- [x] **Quem acessa?**
      → Só `ADMIN_ORG`, fiel ao RF-06 e ao contrato atual dos endpoints.

## Premissas (não questionadas, seguindo o contrato)

- Papéis oferecidos no convite e na alteração: `GESTOR`, `PROFESSOR`, `ALUNO`.
  `ADMIN_ORG` não é atribuível pela interface.
- O criador da organização (owner) não pode ser removido **nem** ter o papel
  alterado — mesma regra do `CannotRemoveOwnerException`.
