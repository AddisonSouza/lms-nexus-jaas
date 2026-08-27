# Login do usuário com mais de uma organização — Delta

## Changed

- **Login escolhe organização sempre que houver vínculo.** Antes só colocava
  `org` no token quando o usuário tinha exatamente uma organização; com duas ou
  mais o token saía sem organização e o `RootRedirect` mandava para `/welcome` —
  tela sem sidebar e, portanto, sem o seletor de organização, deixando o
  multi-organização sem caminho para dentro do app. Agora entra na primeira
  organização por nome.
- **`findOrganizationsByUser` passa a ter ordem estável**: `ORDER BY o.name`,
  a mesma ordem que o seletor da sidebar desenha, para que "a primeira"
  signifique sempre a mesma. A consulta também deixa de trazer organização com
  soft delete.
- **A rotação de token preserva a organização da sessão.** Antes o refresh
  repetia a regra do login e reemitia sem organização, então recarregar a página
  ou o access token expirar desfazia a troca de organização e devolvia o usuário
  a `/welcome`. Agora reemite com a organização em que a sessão está,
  **revalidando o vínculo** — que pode ter sido revogado no meio da sessão — e
  só cai na regra do login quando não há organização válida.

## Added

- O refresh token guarda a organização da sessão: `rt:<token>` no Redis passa a
  valer `userId:organizationId`. Valor sem separador continua sendo sessão sem
  organização, então tokens emitidos antes seguem válidos.
- `RefreshSession(userId, organizationId)` no domínio de `identity`; a porta
  `RefreshTokenRepository` ganha `findSession` e o `organizationId` no `save`.
  O login e a troca de organização gravam onde a sessão está.

## Unchanged

- `/welcome` segue servindo quem realmente não pertence a nenhuma organização.
- Front-end não mudou: com organização no token o `RootRedirect` e o seletor da
  sidebar já funcionam.
- Lembrar a última organização usada entre sessões ficou fora — o login sempre
  entra pela primeira.
