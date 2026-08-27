# Grill Me — 2026-08-26

**Destino após a troca?** → `/`. O `RootRedirect` decide pelo papel novo; a rota
atual pode não ser permitida no contexto novo.

**Quando exibir o seletor?** → Sempre, inclusive com uma organização só ou
nenhuma — ele também é o rótulo de "onde estou" e a porta para criar outra.

**Ajustar o "Voltar" de `/organizations/new`?** → Sim, contextual: quem já tem
organização volta para de onde veio; quem não tem continua indo para `/welcome`.

**Shape do `GET /organizations`?** → `id`, `name` e `role`, só o que o seletor
desenha.
