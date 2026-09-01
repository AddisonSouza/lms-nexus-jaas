# Token do convite se perde no cadastro do usuário convidado — Delta

## Added

- **`GET /invitations/pending`** (autenticado) devolve os convites pendentes e
  não expirados endereçados ao e-mail do usuário do JWT, do mais recente para o
  mais antigo. O e-mail vem do `UserDirectoryPort.findEmailById` e a comparação
  ignora maiúsculas — a mesma regra do aceite (#138). Convite cuja organização
  não existe mais é omitido, porque não teria para onde levar o usuário.
  Camadas novas: `ListPendingInvitationsUseCase`,
  `ListPendingInvitationsService`, `PendingInvitationResponse`,
  `PendingInvitationMapper` e `InvitationRepository.findPendingByEmail`.
- **`RootRedirect` consulta o convite pendente antes de mandar para `/welcome`.**
  É o que faz o convidado recém-cadastrado chegar ao aceite sozinho, mesmo tendo
  confirmado o e-mail em outro navegador — nem a URL nem o `localStorage`
  sobrevivem a esse salto. Só quem chega **sem organização** é consultado
  (`usePendingInvitations(enabled)`): quem já tem organização segue para
  `/classrooms` sem ser desviado por um convite que talvez não queira.
- Testes: `ListPendingInvitationsServiceTest`,
  `ListPendingInvitationsResourceIT` e os testes de `RootRedirect`,
  `LoginPage` e `AcceptInvitePage`.

## Changed

- **O deslogado que abre um convite vai para o login, não para o cadastro.**
  `AcceptInvitePage` mandava para `/register?invite=<token>`, e o parâmetro não
  era lido em lugar nenhum — nem `RegisterPage`, nem `RegisterForm`, nem
  `useRegister` — então o convite morria ali. Agora vai para
  `/login?invite=<token>`: o convidado costuma já ter conta, e quem não tem chega
  ao cadastro pelo link da própria tela de login.
- **O login honra o `?invite=`.** `useLogin` navegava para `/` fixo; agora, com
  convite, volta para `/invitations/<token>/accept` depois de entrar. E o link
  "Criar conta" da `LoginPage` repassa o parâmetro adiante.
- `API_CONTRACT.md`: o novo endpoint documentado e somado à linha do RF-06.

## Unchanged

- O aceite continua explícito, na `AcceptInvitePage` de sempre: o usuário vê
  organização, papel e convidante antes de decidir. Nada é aceito sozinho.
- Regra de confirmação de e-mail e TTL do convite ficaram como estavam.
- Havendo mais de um convite pendente, leva-se ao mais recente — escolher entre
  vários ficou fora de escopo.

## Reported, not fixed

- **Convite por e-mail para turma/disciplina** segue aberto na **#77**; o
  template de e-mail no design orgânico, na **#73**.
- Listar, reenviar ou cancelar convites pelo painel do admin continua sem tela.
