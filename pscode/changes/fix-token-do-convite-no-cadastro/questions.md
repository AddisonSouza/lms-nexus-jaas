# Perguntas

- [x] **Como o convite sobrevive até o login?**
      → Endpoint novo `GET /invitations/pending`, que busca pelo e-mail do JWT.
      Imune a confirmar o e-mail em outro navegador ou aparelho, ao contrário de
      qualquer carregamento só no front.
- [x] **Corrigir junto quem já tem conta?**
      → Sim. Hoje a mesma linha manda todo deslogado para `/register`; passa a
      mandar para `/login`, voltando ao convite depois de entrar.
- [x] **O que acontece na chegada?**
      → A `AcceptInvitePage` que já existe, para o usuário ver organização, papel
      e quem convidou antes de aceitar. Sem aceite automático.
- [x] **Todo login com convite pendente empurra para o aceite?**
      → Não. Dois caminhos: `?invite=<token>` carrega a intenção de `/login` até o
      aceite (resolve quem já tem conta na hora), e a busca por pendentes vale só
      para quem chega **sem organização** — o recém-chegado. Quem já tem
      organização e não veio do link continua caindo em `/classrooms`.
