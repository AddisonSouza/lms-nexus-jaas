# Papel alterado só vale no próximo access token — Delta

## Changed

- **Mudar o vínculo de um membro vale na requisição seguinte, não no próximo
  access token.** A autorização vem do claim `groups` do JWT, e
  `ChangeMemberRoleService` / `RemoveMemberService` gravavam no banco sem tocar
  em token nenhum: um `PROFESSOR` rebaixado a `ALUNO` seguia passando nos
  endpoints de professor por até 15 min. Agora os dois disparam
  `OrganizationMembershipChangedEvent`, o `identity` marca o instante em que as
  sessões daquele usuário ficaram obsoletas, e o access token anterior à marca
  responde **401 `SESSION_STALE`**.
- **O `API_CONTRACT` descrevia o defeito como se fosse a regra** ("só passa a
  agir com o novo papel no próximo login ou troca de organização"). Os dois
  endpoints de membro passam a dizer que vale na requisição seguinte, e uma
  convenção global explica o 401 que qualquer endpoint pode responder.

## Added

- `OrganizationMembershipChangedEvent(userId, organizationId)` — evento CDI, no
  mesmo formato com que `assessment` já fala com `communication`. Nasce genérico
  para um futuro "sair da organização por conta própria" só precisar disparar:
  hoje remover é ação de `ADMIN_ORG` e não existe esse caminho.
- `StaleSessionRepository` + `StaleSessionRedisRepository`
  (`identity:stale-since:{userId}`), com o TTL do access token — passado ele,
  nenhum token anterior à marca ainda vive e a chave some sozinha.
- `MarkSessionsStaleOnMembershipChanged`: quem cuida de sessão é o `identity`, e
  é ele que observa e escreve a marca.
- `StaleSessionFilter`: 401, **não 403**, de propósito — é o 401 que o
  interceptor do front (`axios.ts:29`) trata renovando o token em silêncio e
  refazendo a requisição, e a rotação relê o papel do banco. O refresh token não
  é tocado: ninguém é deslogado.
- `REQ-AUTH-10` na spec viva de `authentication`, mais `StaleSessionIT` (3
  casos), `StaleSessionFilterTest` (5), `MarkSessionsStaleOnMembershipChangedTest`
  e os disparos cobertos nos testes dos dois services.

## Unchanged

- Nada mudou no front. O 401 já era tratado, e o `ProtectedRoute` já barra por
  papel — daí a rota sem guard (`/curriculum`) mostrar o erro do 403 em vez de
  redirecionar, como estava combinado.
- O TTL do access token, e quem pode alterar o papel de quem (regra da #139).
- Invalidar sessões ao resetar senha (`REQ-AUTH-08`) continua como estava.

## Notas de implementação

- O filtro é `@ServerRequestFilter`, e não `ContainerRequestFilter`: o
  `ExecutionModelAnnotationsProcessor` recusa `@Blocking` em método que
  implementa interface. Ficou sem `@Blocking` — os endpoints são todos
  bloqueantes, então o request já corre em worker thread, e a IT confirma.
- O `iat` tem precisão de segundo: um token emitido no mesmo segundo da marca
  passa. Janela residual abaixo de um segundo, sem risco de laço — o `_retry` do
  interceptor garante uma tentativa só.
- O evento é disparado dentro da transação; se ela reverter, a marca fica e o
  usuário renova o token à toa. Mesmo comportamento dos eventos que já existem.

## Reported, not fixed

- **`/auth/` não é isento do filtro.** Com autenticação proativa, o Quarkus
  popula o principal sempre que um Bearer chega, então um cliente que mandasse o
  access token junto do `POST /auth/refresh` teria o próprio refresh barrado.
  Hoje não é defeito: o interceptor chama o refresh com `axios` puro, sem header,
  e o `switch-organization` se recupera sozinho (401 → renova → repete).
  Decidido deixar como está.
