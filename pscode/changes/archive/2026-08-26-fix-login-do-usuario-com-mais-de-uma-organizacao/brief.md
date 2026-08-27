## Objetivo

Quem pertence a mais de uma organização precisa conseguir entrar no app — hoje
ele fica ilhado numa tela que afirma que ele não pertence a nenhuma.

## Comportamento esperado

O login coloca uma organização no token sempre que o usuário tiver algum
vínculo, e não só quando tiver exatamente um. Com mais de uma, entra na primeira
por nome — a mesma ordem do seletor da sidebar — e de lá ele troca à vontade.

A sessão passa a lembrar em qual organização está: recarregar a página ou o
access token expirar não devolve mais um token sem organização, o que hoje
desfaz a troca e joga o usuário de volta para `/welcome`.

`/welcome` continua existindo para quem realmente não pertence a nenhuma
organização.

## Fora de escopo

O seletor de organização na sidebar (#104, entregue), convites e criação de
organização.
