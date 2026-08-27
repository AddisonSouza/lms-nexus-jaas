## Objetivo

Dar ao usuário que pertence a mais de uma organização uma forma de ver em qual
está e trocar de contexto sem deslogar, e um atalho para fundar outra.

## Comportamento esperado

No topo da sidebar, um seletor mostra o avatar de iniciais, o nome da organização
ativa e o papel do usuário nela. Ao abrir, um card "Suas organizações" lista as
organizações do usuário — a ativa marcada, as demais com a tag do papel — e traz
"Criar organização" ao pé. Escolher outra organização troca o contexto e a UI
passa a refletir o papel novo.

Depende de um endpoint novo, `GET /organizations`, devolvendo as organizações do
usuário autenticado com o papel em cada uma. O `POST /auth/switch-organization`
já existe.

## Fora de escopo

Tela de Membros e convites pendentes; link aberto de convite de organização;
qualquer mudança no fluxo de convite por e-mail.
