# fix: diálogos não fecham após sucesso — Delta

## Changed
- **`PATCH /submissions/{id}/evaluation`** devolve `createdAt` e `updatedAt`
  preenchidos. O `createdAt` saía nulo: a coluna é `updatable = false`, então o
  banco preservava o valor, mas o `merge` copiava o null da entidade destacada
  para a gerenciada — e era dela que a resposta era montada.
  `SubmissionRepositoryImpl.save` relê o estado persistido antes de mapear.
- **`POST /classrooms/{id}/members`** devolve `joinedAt` preenchido. O
  repositório devolvia o domínio de entrada, sem o valor que nasce no
  `@PrePersist`. Agora devolve o membro gravado.
  - O `joinedAt` da resposta passa a ser **idêntico** ao que a listagem devolve.
    Só o flush traria os nanossegundos do `LocalDateTime.now()`, que a coluna não
    guarda — e o cache do TanStack Query ficaria com um valor que nenhum GET
    confirma.
- **Consequência nas telas** (o front já estava correto, nada mudou nele): com
  timestamps válidos o Zod aceita a resposta, a mutation entra em `onSuccess` e
  o diálogo fecha invalidando a lista. Antes a operação era salva, o diálogo
  ficava aberto, a lista só atualizava com reload e o segundo clique do professor
  batia num 422 silencioso (`SUBMISSION_ALREADY_EVALUATED`).

## Added
- `EvaluateSubmissionResponseIT`: a resposta traz os timestamps, e avaliar não
  reescreve a data de entrega do aluno.
- `AddClassroomMemberResponseIT`: a resposta traz o `joinedAt`, e ele coincide
  com o da listagem do mesmo membro.
- `SubmissionListDrawer.test.tsx` e `ClassroomMembersPanel.test.tsx`, cobrindo o
  que o usuário vê: o diálogo fecha, a query da lista é invalidada, uma recusa da
  API mantém o diálogo aberto com o texto digitado, e o botão de envio fica
  desabilitado enquanto a requisição está em voo — uma chamada só.

## Removed
- Nada. Sem mudança de schema, de contrato ou de front.

## Conhecido, fora deste card
- Os diálogos ainda não **exibem** a mensagem de erro da API quando ela recusa —
  eles apenas permanecem abertos. Isso é o card `erros-4xx-sem-feedback-na-ui`.
- Adicionar membro segue pedindo o UUID digitado; a seleção por lista tem card
  próprio.
- Outros repositórios do projeto podem ter o mesmo padrão (devolver o domínio de
  entrada em vez do persistido). Só os dois caminhos deste card foram revisados.
