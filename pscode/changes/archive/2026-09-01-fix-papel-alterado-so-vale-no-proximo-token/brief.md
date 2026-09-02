# Papel alterado só vale no próximo access token

## Objetivo

Quando um `ADMIN_ORG` altera o papel de um membro, a mudança precisa valer na
hora. Hoje vale só quando o access token do afetado é renovado — até 15 minutos
depois.

## Comportamento esperado

`PATCH /organizations/{id}/members/{userId}` grava o papel novo no banco, mas a
autorização de cada endpoint vem do claim `groups` do JWT, e o token em
circulação não é tocado. Um `PROFESSOR` rebaixado a `ALUNO` continua passando
nos endpoints de professor até o token expirar.

A janela se fecha sozinha e não exige re-login: `ACCESS_TOKEN_TTL` é de 15 min e
o `RefreshTokenService` relê o papel do banco ao rotacionar. O que falta é
decidir se essa janela é aceitável ou se deve ser fechada — e implementar a
decisão.

## Fora de escopo

- O TTL do access token como decisão global de sessão.
- Rever quem pode alterar papel de quem (regra atual da #139 permanece).

## Origem

Levantado durante a #139, onde a janela foi documentada no `API_CONTRACT.md`
mas não tratada.
