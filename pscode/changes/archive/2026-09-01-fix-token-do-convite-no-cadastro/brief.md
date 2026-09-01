# Token do convite se perde no cadastro do usuário convidado

## Objetivo

Fazer o token do convite de organização sobreviver ao cadastro, de modo que quem
é convidado e ainda não tem conta chegue até o aceite sem precisar voltar ao
e-mail.

## Comportamento esperado

Ao abrir o link do convite sem estar logado, o usuário é levado ao cadastro;
depois de criar a conta, confirmar o e-mail e entrar, ele volta automaticamente
para a tela de aceite daquele convite.

## Estado atual (confirmado na revisão da #78)

`AcceptInvitePage.tsx:39` redireciona para `/register?invite=<token>`, mas o
parâmetro `invite` não é lido em nenhum ponto do front — nem `RegisterPage`, nem
`RegisterForm`, nem o fluxo de confirmação de e-mail. O token é descartado no
cadastro. Como a conta nasce em `PENDING_CONFIRMATION` e o login só passa depois
da confirmação, o convidado sem conta — o caso mais comum de convite — perde o
convite no meio do caminho.

## Fora de escopo

- Mudanças na regra de confirmação de e-mail.
- Convite por e-mail para turma.
- Template de e-mail (#73).

## Origem

Separado da #78 para não misturar as correções de isolamento e contrato do
back-end com a reescrita do fluxo de entrada. Relacionado à #77, que vai mexer
nessa mesma área.
