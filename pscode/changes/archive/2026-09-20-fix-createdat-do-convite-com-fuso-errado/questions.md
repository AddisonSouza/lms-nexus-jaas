# Perguntas — fix: createdAt do convite com fuso errado

- [x] **Alcance da correção de fuso?** → Só o convite. O `@PrePersist` do
  `InvitationJpaEntity` é o único ponto onde hora local cruza com um mapper que
  assume UTC. Padronizar toda a API em `Instant` fica para outra card.
- [x] **`createdAt: null` no `PUT` de submissão entra nesta card?** → Sim. Causa
  distinta (o `em.merge()` com `toEntity` ignorando `createdAt`), mas o issue
  pede a verificação e o conserto é pequeno.
- [x] **Cobertura de teste?** → Teste de integração com a JVM em fuso não-UTC
  (`America/Sao_Paulo`), verificando que `expiresAt - createdAt` é exatamente
  7 dias.
- [x] **`joinedAt` de membros tem o mesmo bug?** → Não. É `LocalDateTime` ponta a
  ponta (entidade → domínio → DTO → JSON sem `Z`), consistente com o que o
  front espera. Fica fora do escopo.
