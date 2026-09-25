# Grill Me
- [x] Incluir `SubjectJpaEntity` além das tabelas de junção? — Sim, entra neste card.
- [x] Como substituir as subqueries do reporting? — Buscar os ids pelo port e usar `IN :ids`; com lista vazia, curto-circuito.
- [x] Quem traduz userId → memberId? — O port do curriculum, via o `OrganizationMemberQueryPort` que ele já tem.
