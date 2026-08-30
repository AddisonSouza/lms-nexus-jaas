# Grill Me — revisão dos convites

- [x] **Join de turma entre organizações diferentes?** → **Bloquear com 404.** O
  join só encontra turma da organização do JWT; código de outra org devolve
  `INVALID_INVITE_CODE`, sem revelar que a turma existe.
- [x] **Token de convite perdido no cadastro (`?invite=`) entra no escopo?** →
  **Sim.** É o caso mais comum de convite — pessoa sem conta — e hoje está
  quebrado.
- [x] **Aceitar convite exige que o e-mail do convite seja o do usuário logado?**
  → **Sim, validar.** Hoje qualquer autenticado com o token ganha o papel.
- [x] **Demais defeitos confirmados entram todos?** → **Sim, os quatro:** código
  vazando no join, mensagens de erro no front, `setToken`/`clear` após aceitar,
  e 200 para já-membro.

## Abertas

- [ ] Nenhuma — escopo fechado.
