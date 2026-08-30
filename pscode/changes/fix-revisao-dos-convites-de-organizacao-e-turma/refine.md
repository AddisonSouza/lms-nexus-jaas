# Revisão dos convites de organização e turma

## Summary

Os dois caminhos de entrada — convite de organização por link e entrada em turma
por código — foram revisados ponta a ponta na stack local. Sete defeitos
confirmados, sendo o mais grave um furo de isolamento entre organizações: um
usuário entra numa turma de organização à qual não pertence. Esta change corrige
os sete.

## Technical detail

Verificado com API, banco e front rodando. Reproduções feitas por `curl` com
token real e conferidas no banco.

**Back-end**

- **Join atravessa organizações.** `JoinClassroomService:27` resolve a turma por
  `findByInviteCode` sem filtro de organização e nunca checa
  `isUserInOrganization` — guarda que existe no caminho do admin
  (`AddClassroomMemberService`) mas não no self-service. Reproduzido: usuário da
  "Org Repro 74" entrou numa turma da "Escola Teste" com **HTTP 201**, criando
  linha em `classroom_members` com o `organization_id` da outra org. Viola
  "`organization_id` sempre do JWT" (DECISIONS.md).
- **Código de convite vaza.** `JoinClassroomService:37,51` usa o `toResponse` de
  um argumento, sem filtro por papel, e devolve `inviteCode` a quem acabou de
  entrar. A spec `classroom-management` exige `inviteCode: null` para `ALUNO`.
- **Já-membro devolve 201.** `ClassroomResource:179` responde `CREATED` sempre,
  embora o próprio `@APIResponse` e o `API_CONTRACT.md` documentem 200.
- **Aceite não confere o e-mail.** `AcceptInviteService` valida pendente,
  expirado e já-membro, mas não que o convite era para aquele usuário: qualquer
  autenticado com o token ganha o papel. O módulo já alcança `users`
  (`existsActiveMemberByEmail`); o caminho limpo é um `UserDirectoryPort`, como o
  do módulo `classroom`.

**Front-end**

- **Aceitar convite não troca o token.** `useAcceptInvitation.ts:10` navega para
  `/organizations/{id}` sem `setToken` nem `queryClient.clear()`, diferente de
  `useSwitchOrganization` e `useCreateOrganization`. O usuário cai numa
  organização que o JWT dele não tem — mesmo padrão que gerou as #74/#75.
- **Token some no cadastro.** `AcceptInvitePage.tsx:39` manda quem não está
  logado para `/register?invite=<token>`, mas `invite` não é lido em lugar
  nenhum. Como o cadastro exige confirmação de e-mail, o convidado sem conta —
  o caso mais comum — perde o convite no caminho.
- **Erro genérico no join.** `JoinClassroomForm.tsx:46` mostra uma frase única,
  embora o back-end devolva `errorCode` estável (`INVALID_INVITE_CODE` 404,
  `CLASSROOM_ARCHIVED` 422).

## Scope

### In
- Join restrito à organização do JWT, respondendo 404 para código de fora.
- Join sem devolver `inviteCode`, e 200 quando o usuário já é membro.
- Aceite de convite validando o e-mail do convite contra o do usuário logado.
- `useAcceptInvitation` trocando o token e limpando o cache.
- Mensagens de erro do join derivadas do `errorCode`.
- Testes de integração e de front dos cenários acima; `API_CONTRACT.md` e specs.

### Out
- **Token de convite sobrevivendo a cadastro → confirmação → login.** Defeito
  confirmado (`?invite=` é descartado), mas separado numa issue própria ligada à
  #77, que vai reescrever esse fluxo.
- Novos canais de convite (e-mail em massa, SMS) e o convite por e-mail para
  turma — é a #77.
- Redesign das telas de convite e o template de e-mail — é a #73.
- Convite no nível de disciplina (`Subject`): não existe hoje, fica reportado.
- Trocar o código de 6 caracteres por outro formato, ou limitar tentativas.

## Subtasks

- [x] Restringir o join à organização do JWT: `findByInviteCode` com `orgId`,
      `JoinClassroomCommand` e `ClassroomResource` passando o claim
- [x] Join deixa de devolver `inviteCode` (filtro por papel) e responde 200
      quando o usuário já era membro
- [x] Aceite de convite valida o e-mail: `UserDirectoryPort` no módulo
      `organization`, adapter e exceção própria
- [x] Testes de integração: join fora da org, código não vazando, já-membro 200
      e aceite com e-mail divergente
- [x] `useAcceptInvitation` chama `setToken` e `queryClient.clear()` após aceitar
- [x] `JoinClassroomForm` deriva a mensagem do `errorCode` do back-end
- [x] Atualizar `API_CONTRACT.md` e as specs; rodar lint, type-check e a suíte;
      validar os dois fluxos no browser
