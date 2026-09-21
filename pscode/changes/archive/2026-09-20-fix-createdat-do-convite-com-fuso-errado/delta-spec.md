# fix: createdAt do convite com fuso errado — Delta

## Changed

- **`createdAt` do convite passa a ser o instante real do envio (RF-06).** O
  `@PrePersist` de `InvitationJpaEntity` sobrescrevia com `LocalDateTime.now()`
  — o relógio local — o valor que `InvitationMapper.toEntity` já havia gravado
  em UTC; na leitura, `toDomain` relia aquela hora local como se fosse UTC. Com
  a JVM em UTC-3, um convite enviado às 00:32 era listado como "Enviado em" três
  horas antes, no dia anterior. O callback agora só preenche `createdAt` quando
  ele está ausente, e o fallback usa `LocalDateTime.now(ZoneOffset.UTC)`, o
  mesmo referencial que a leitura assume. `expiresAt` nunca passou pelo
  callback, então `expiresAt - createdAt` volta a ser os 7 dias que o serviço
  define. Sem mudança de schema, sem Flyway.
- **`SubmissionMapper.toEntity` deixa de ignorar `createdAt`/`updatedAt`.** A
  entidade destacada leva os timestamps do domínio para o `merge` em vez de
  nulos; o `@PrePersist` segue como fallback na criação. Defesa em profundidade,
  sem efeito observável: o `em.refresh` de `SubmissionRepositoryImpl.save`
  (#368) já relê o estado persistido antes de responder.

## Added

- **`InvitationCreatedAtTimeZoneIT`** prende a JVM em `America/Sao_Paulo`, cria
  o convite pelo REST e afirma que o `createdAt` devolvido cai na janela real do
  envio e que `expiresAt - createdAt` é de 7 dias. Em UTC o defeito é invisível,
  daí o fuso forçado. Verificado nos dois sentidos: sem a correção o teste falha,
  com o `createdAt` três horas antes da janela.

## Removed

- Nada.

## Conhecido, fora deste card

- **Convites já gravados continuam com o fuso errado.** Migrar os dados
  existentes ficou fora de escopo; no banco de QA a diferença aparece como 7
  dias + 3h entre `created_at` e `expires_at` nas linhas antigas.
- **As demais entidades seguem em `LocalDateTime` local** (tarefas, turmas,
  avisos, notificações, usuários). Padronizar `Instant`/UTC é card própria.
- `joinedAt` de membros foi verificado e é consistente — grava e lê no mesmo
  referencial local, sem rótulo de UTC.
- A correção da submissão chegou redundante: o PR #368 resolveu o
  `createdAt: null` em `main` depois que este card foi refinado. Mantida por
  decisão do dev, como mapeamento fiel ao domínio.
