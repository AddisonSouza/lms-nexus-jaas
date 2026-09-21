# fix: createdAt do convite com fuso errado

## Summary

Um convite criado às 00:32 aparece na lista como "Enviado em 14/09" em vez de
15/09. A data que a API devolve está três horas atrasada porque o back-end grava
a hora do relógio local e depois a lê como se fosse UTC. Esta correção faz o
`createdAt` do convite voltar a bater com o instante real do envio, e de quebra
corrige a resposta do `PUT` de submissão, que devolve `createdAt: null`.

## Technical detail

- **Causa-raiz.** `InviteMemberService` já monta o convite com
  `createdAt(Instant.now())` — correto. Mas
  `InvitationJpaEntity.prePersist()` (`:46`) sobrescreve o campo com
  `LocalDateTime.now()`, que é hora local (UTC-3). Na leitura,
  `InvitationMapper.toDomain` faz `entity.getCreatedAt().toInstant(ZoneOffset.UTC)`
  e rotula aquela hora local como UTC. `expiresAt` não passa pelo `@PrePersist`,
  então permanece correto — daí os 3h de diferença (`00:32Z` vs `03:32Z`).
- **Correção.** O `@PrePersist` deixa de sobrescrever um `createdAt` já
  preenchido; quando precisar do fallback, usa `LocalDateTime.now(ZoneOffset.UTC)`,
  o mesmo referencial do mapper. Nenhuma mudança de schema, nenhum Flyway.
- **Submissão (`createdAt: null`).** Causa diferente: `SubmissionMapper.toEntity`
  declara `@Mapping(target = "createdAt", ignore = true)`, então o
  `em.merge()` em `SubmissionRepositoryImpl.save` devolve a entidade gerenciada
  com o campo nulo. O banco fica íntegro (`updatable = false`), só a resposta
  sai errada. Basta mapear `createdAt`/`updatedAt` do domínio e manter o
  `@PrePersist` como fallback para a criação.
- **`joinedAt` de membros — sem bug.** É `LocalDateTime` da entidade ao JSON, sem
  sufixo `Z`; o front o interpreta como hora local, que é como foi gravado.
- **Teste.** Integração com Testcontainers e a JVM em `America/Sao_Paulo`,
  afirmando que `expiresAt - createdAt` é exatamente 7 dias.

## Scope

### In

- `@PrePersist` de `InvitationJpaEntity`: não sobrescrever valor existente e usar
  referencial UTC.
- `SubmissionMapper.toEntity`: mapear `createdAt`/`updatedAt` para o `merge` não
  zerá-los.
- Teste de integração do convite com fuso não-UTC forçado.

### Out

- Migrar ou corrigir convites já gravados com o fuso errado.
- Padronizar `Instant`/UTC nas demais entidades (tarefas, turmas, avisos,
  notificações, usuários) — vira card própria.
- `joinedAt` de membros: verificado, consistente, não muda.
- Qualquer alteração de schema ou de front-end.

## Subtasks

- [ ] Corrigir o `@PrePersist` de `InvitationJpaEntity` para preservar o `createdAt` do domínio e usar referencial UTC no fallback
- [ ] Mapear `createdAt`/`updatedAt` em `SubmissionMapper.toEntity` para o `merge` não devolver `createdAt: null`
- [ ] Adicionar teste de integração do convite com a JVM em fuso não-UTC verificando `expiresAt - createdAt == 7 dias`
