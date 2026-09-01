# Papel alterado só vale no próximo access token

## Summary

Quando um `ADMIN_ORG` muda o papel de um membro — ou o remove da organização —
a decisão só vale de fato até 15 minutos depois, quando o token do afetado é
renovado. Esta mudança faz valer na hora, sem deslogar ninguém e sem o usuário
perceber.

## Technical detail

**Por que a janela existe**

- A autorização de cada endpoint vem do claim `groups` do JWT (`@RolesAllowed`,
  e `isAdminOf` em `OrganizationResource:135`). `ChangeMemberRoleService` e
  `RemoveMemberService` gravam no banco e não tocam em nenhum token.
- `JwtTokenService.ACCESS_TOKEN_TTL` é de 15 min. Só na rotação o
  `RefreshTokenService` relê o papel do banco — aí a janela se fecha sozinha.
- **Invalidar o refresh token não resolveria**: o access token em circulação
  continuaria válido pelo tempo todo, e a renovação seguinte falharia com 401,
  caindo no `clearToken()` do interceptor (`axios.ts:57`) — janela intacta e
  usuário no login. Descartado.

**Como fechar**

- Todo ponto que muda o vínculo do usuário com a organização dispara
  `OrganizationMembershipChangedEvent(userId)` — evento CDI, como
  `TaskPublishedEvent` já cruza de `assessment` para `communication`.
- `identity` observa e marca no Redis o instante em que as sessões daquele
  usuário ficaram obsoletas: chave `identity:stale-since:<userId>`, TTL igual ao
  do access token. Passados 15 min nenhum token anterior à marca ainda vive, e a
  chave some sozinha.
- Um `ContainerRequestFilter` novo em `identity/infrastructure/security` compara
  o `iat` do JWT com a marca e aborta com **401** quando o token é anterior.
  Requisição sem JWT passa direto — o filtro não fala sobre endpoint público.
- O **401 é o ponto todo**: o interceptor do front (`axios.ts:29`) já renova em
  silêncio e refaz a requisição original, e o `RefreshTokenService` relê o papel
  do banco. O refresh token não é tocado, então ninguém é deslogado. O `_retry`
  do interceptor garante uma tentativa só — não há laço possível.

**Arestas**

- O `iat` tem precisão de segundo: um token emitido no mesmo segundo da marca
  passa. Janela residual abaixo de um segundo, sem risco de laço.
- Um GET no Redis por requisição autenticada, enquanto a marca existir.
- O evento é disparado dentro da transação; se ela reverter, a marca já foi
  escrita e o usuário renova o token à toa. Mesmo comportamento dos eventos que
  já existem.
- Não há hoje endpoint de sair da organização por conta própria — remover é ação
  de `ADMIN_ORG`. O evento nasce com nome genérico para esse caminho futuro só
  precisar disparar.

## Scope

### In

- `OrganizationMembershipChangedEvent` disparado por `ChangeMemberRoleService` e
  `RemoveMemberService`.
- Porta e repositório Redis da marca de sessão obsoleta, em `identity`.
- Filtro que responde 401 ao access token anterior à marca.
- Testes unitários dos dois services e de integração do fluxo completo.
- `API_CONTRACT.md` e a spec viva de `authentication`.

### Out

- Qualquer mudança no front: o 401 já é tratado, e o `ProtectedRoute` já barra
  por papel na próxima navegação.
- O TTL do access token como decisão global de sessão.
- Rever quem pode alterar o papel de quem (regra da #139 permanece).
- Invalidar sessões em outros eventos: o reset de senha já derruba as suas, e
  isso continua como está.

## Subtasks

- [x] BE: `OrganizationMembershipChangedEvent` disparado por `ChangeMemberRoleService` e `RemoveMemberService`, com os testes dos dois services
- [ ] BE: porta `StaleSessionRepository` + implementação Redis (`identity:stale-since:<userId>`, TTL do access token)
- [ ] BE: observer em `identity` que marca as sessões do usuário como obsoletas
- [ ] BE: `StaleSessionFilter` responde 401 ao token anterior à marca e ignora requisição sem JWT
- [ ] BE: teste de integração — papel alterado e membro removido passam a valer na requisição seguinte
- [ ] Docs: `API_CONTRACT.md` (a nota da janela de 15 min do RF-06) e a spec viva de `authentication`
