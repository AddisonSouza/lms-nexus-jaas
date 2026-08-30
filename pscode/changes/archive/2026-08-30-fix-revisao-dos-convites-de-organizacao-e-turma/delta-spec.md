# Revisão dos convites de organização e turma — Delta

## Changed

- **O código de convite da turma não atravessa mais organizações.** Antes
  `findByInviteCode` resolvia o código sem filtro algum e o join nunca checava o
  vínculo de quem entrava: qualquer autenticado entrava numa turma de qualquer
  organização, criando linha em `classroom_members` com o `organization_id`
  alheio — reproduzido com HTTP 201 antes da correção. Agora o código só resolve
  dentro da organização do JWT, e um código de fora responde **404
  `INVALID_INVITE_CODE`**, sem revelar que a turma existe em outro lugar.
- **O join deixou de devolver o `inviteCode`.** Quem entra por código entra como
  `ALUNO`, e o `ALUNO` nunca recebe o código de volta (RF-08) — a mesma regra que
  listar e detalhar já aplicavam. Vale também ao repetir o ingresso.
- **O join responde 200 quando o usuário já era membro**, como o `API_CONTRACT`
  documentava; antes respondia 201 sempre.
- **O aceite de convite confere o destinatário.** Validava pendente, expirado e
  já-membro, mas não que o convite era daquela pessoa: bastava ter o token. O
  e-mail passa a ser comparado sem diferenciar caixa; divergência responde **403
  `INVITATION_NOT_FOR_THIS_USER`**, e um usuário desconhecido cai no mesmo 403.
- **Aceitar um convite entra na organização.** `useAcceptInvitation` navegava sem
  trocar o token nem limpar o cache, então o JWT seguia na organização anterior
  (ou em nenhuma) e a tela de destino respondia 403 — mesmo padrão das #74/#75.
  Agora reemite o token e limpa o cache antes de navegar, e não mexe na sessão
  quando o aceite falha.
- **A mensagem de erro do join vem do `errorCode`** em vez de uma frase única.
- **O código de convite é normalizado antes de validar.** O campo só aparentava
  caixa alta por CSS, então um código correto digitado em minúsculas era recusado
  pelo próprio formulário, sem nem chamar a API.

## Added

- `JoinClassroomResult` separa "entrou agora" de "já era membro", para o resource
  escolher 201 ou 200 sem adivinhar.
- `UserDirectoryPort` + `UserDirectoryAdapter` no módulo `organization`, no mesmo
  padrão do módulo `classroom`: lê o e-mail do usuário sem depender das internas
  do `identity`. E `Invitation.isAddressedTo` como regra de domínio.
- `src/lib/session.ts` recebe `switchOrganization`, que é sessão e não
  organização. Sem isso, `useAcceptInvitation` precisaria importar de
  `features/organization` — o primeiro import de feature para feature do projeto,
  que o CLAUDE.md proíbe e o lint não pegaria (a regra só barra imports relativos).
- `JoinClassroomIsolationIT` e `AcceptInviteEmailMatchIT` (5 casos), testes do
  `JoinClassroomForm` (4) e do `useAcceptInvitation` (2). Requirements novos em
  `classroom-join-by-code` e `member-invitations`.

## Unchanged

- O `API_CONTRACT` do join já documentava 200/201 — era o código que divergia. Já
  o do aceite estava desatualizado (dizia `201`/`400` quando o endpoint sempre
  respondeu `204`), e foi corrigido junto.
- O código de 6 caracteres continua como está: trocar o formato ou limitar
  tentativas ficou de fora.

## Reported, not fixed

- **Token de convite descartado no cadastro** (`?invite=` não é lido em lugar
  nenhum): separado na **#130**, ligada à #77.
- **RF-06 sem interface**: o back-end de convidar e remover membros da
  organização existe, mas nenhuma tela o alcança, e alterar papel de membro não
  tem endpoint. Registrado na **#139**.
- Convite no nível de disciplina (`Subject`) não existe.
