# Grill Me — 2026-08-26

**Qual organização o login escolhe quando há várias?** → A primeira por nome,
mesma ordem do seletor. O usuário já cai dentro do app e troca pela sidebar.

**O refresh entra nesta issue?** → Sim. O `RefreshTokenService` repete a regra
do `size() == 1`, então cada reload (ou expiração do access token) devolve um
token sem organização e desfaz a troca. Sem isso o conserto do login fica pela
metade.

**Onde guardar a organização da sessão?** → Junto do refresh token no Redis.
Vir do request body violaria a regra de `organization_id` sempre sair do
servidor, nunca do cliente.
