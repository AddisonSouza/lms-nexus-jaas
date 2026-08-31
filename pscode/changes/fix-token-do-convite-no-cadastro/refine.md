# Token do convite se perde no cadastro do usuário convidado

## Summary

Quem é convidado para uma organização e ainda não tem conta perde o convite no
meio do caminho: o cadastro descarta o token e não há como voltar ao aceite sem
reabrir o e-mail. Esta mudança faz o convidado chegar ao aceite sozinho, tenha
ele conta ou não — inclusive quando confirma o e-mail em outro aparelho.

## Technical detail

**Onde o convite se perde hoje**

- `AcceptInvitePage.tsx:39` manda todo deslogado para `/register?invite=<token>`.
  O parâmetro não é lido em lugar nenhum: nem `RegisterPage`, nem `RegisterForm`,
  nem `useRegister` (que faz `navigate('/confirm-email')` sem repassar nada).
- Entre o cadastro e o login existe **o clique no link de confirmação, vindo da
  caixa de e-mail** — possivelmente em outro navegador. Por isso carregar o token
  pela URL ou por `localStorage` não basta.
- `useLogin` faz `navigate('/')` fixo; não existe hoje nenhum "voltar para onde
  eu estava".

**Os dois caminhos**

1. **Quem já tem conta** — `AcceptInvitePage` passa a mandar para
   `/login?invite=<token>` (não mais `/register`). `LoginPage` repassa o
   parâmetro no link "Criar conta", e depois de entrar o usuário volta para
   `/invitations/<token>/accept`.
2. **Quem acabou de se cadastrar** — endpoint novo `GET /invitations/pending`
   (autenticado) devolve os convites pendentes e não expirados endereçados ao
   e-mail do JWT. `RootRedirect` consulta esse endpoint **apenas quando o usuário
   chega sem organização** — hoje o ramo que vai para `/welcome` — e leva ao
   aceite se houver convite. Quem já tem organização e não veio do link continua
   indo para `/classrooms`.

**Back-end**

- `InvitationRepository` ganha a busca por e-mail + status pendente + não
  expirado; a resposta traz token, nome da organização, papel e quem convidou,
  reaproveitando o que `GetInvitationInfoService` já monta.
- O e-mail vem do JWT via `UserDirectoryPort.findEmailById`, comparado sem
  diferenciar maiúsculas (mesma regra do aceite, #138).

**Chegada**

A `AcceptInvitePage` atual, para o usuário ver organização, papel e convidante
antes de aceitar. Sem aceite automático.

## Scope

### In

- `GET /invitations/pending` + repositório, use case e testes.
- `AcceptInvitePage`: deslogado vai para `/login?invite=<token>`.
- `LoginPage`/`useLogin`: honrar `?invite=` e repassá-lo ao link de cadastro.
- `RootRedirect`: sem organização, checar convite pendente antes de `/welcome`.
- Contrato do novo endpoint no `API_CONTRACT.md`.

### Out

- Regra de confirmação de e-mail e TTL do convite.
- Convite por e-mail para turma e a reescrita do fluxo (#77).
- Template de e-mail (#73).
- Listar, reenviar ou cancelar convites pelo painel do admin.
- Escolher entre vários convites pendentes: leva-se ao mais recente.

## Subtasks

- [x] BE: `GET /invitations/pending` — repositório, use case, endpoint e testes
- [x] Docs: registrar o endpoint no `API_CONTRACT.md` (RF-06)
- [ ] FE: `AcceptInvitePage` manda o deslogado para `/login?invite=<token>`
- [ ] FE: `LoginPage`/`useLogin` honram `?invite=` e repassam ao link de cadastro
- [ ] FE: `RootRedirect` leva ao convite pendente quando o usuário chega sem organização
- [ ] Validar o golden path na aplicação (convite → cadastro → confirmação → login → aceite)
