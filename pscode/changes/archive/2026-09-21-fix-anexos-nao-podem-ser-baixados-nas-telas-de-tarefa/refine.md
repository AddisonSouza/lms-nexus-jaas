# fix: anexos não podem ser baixados nas telas de tarefa

## Summary

Hoje nenhum anexo do produto é baixável: o aluno não vê os arquivos da tarefa,
o professor vê só o nome do arquivo da submissão, e o link de conteúdo da
disciplina abre uma aba com 401 porque não leva o JWT. Esta change adiciona um
botão de download autenticado nas três telas e faz a API devolver o arquivo com
o tipo e o nome originais.

## Technical detail

- **Download autenticado.** Novo `apps/web/src/lib/download.ts` com
  `downloadFile(fileKey, originalName)`: `api.get('/files/<key>', {
  responseType: 'blob' })`, `URL.createObjectURL`, `<a download>` sintético e
  `revokeObjectURL`. Reusa o interceptor de token e o refresh 401 do `@lib/axios`
  — mesmo padrão já usado em `exportAdminDashboardPdf`.
- **Rota normalizada.** `FileResource` é a única resource com prefixo `/api`
  (`@Path("/api/files")`); passa a `@Path("/files")`, como `/tasks` e
  `/classrooms`. Único consumidor é o `ContentCard`, tocado aqui.
- **Resposta do arquivo.** `ServeFileUseCase` passa a devolver o `StoredFile`
  além do stream (`mimeType`/`originalName` vêm do storage) e `FileResource`
  responde com `Content-Type` real e
  `Content-Disposition: attachment; filename="..."`. Chave inexistente
  (`NoSuchKeyException`) vira 404 via `FileNotFoundException` do domínio +
  mapper, em vez do 500 atual.
- **CORS.** `Content-Disposition` precisa entrar em
  `quarkus.http.cors.exposed-headers` para o browser enxergar o cabeçalho.
- **Dados já chegam.** `taskSchema`/`submissionSchema` em `submissions.ts` já
  trazem `attachments[]` com `fileKey`, `originalName` e `mimeType`; falta só
  renderizar.

## Scope

### In
- `downloadFile` + componente `AttachmentLink` (ícone, nome, tamanho) com testes.
- `StudentTaskListPage`: linha de anexos no card, abaixo do prazo.
- `EvaluationDialog` e `SubmissionListDrawer`: anexo da submissão baixável
  (o drawer troca "N anexo(s)" pela lista).
- `ContentCard`: o `<a href>` do arquivo vira download autenticado; link
  externo (`externalUrl`) continua `<a target="_blank">`.
- `FileResource`/`ServeFileUseCase`: rota `/files`, `Content-Type`,
  `Content-Disposition`, 404; `API_CONTRACT.md` e spec `file-storage`.

### Out
- Upload e pré-visualização no navegador.
- Validação de organização no `GET /files/{fileKey}` (gap conhecido da spec —
  card próprio: o `fileKey` não carrega `organization_id`).
- Nome do aluno no `SubmissionListDrawer`.
- Anexos de aviso (`AnnouncementCard`) — mesma correção, outro card.

## Subtasks
- [x] `lib/download.ts` com `downloadFile(fileKey, originalName)` e teste unitário (blob, nome, revoke)
- [x] `components/shared/AttachmentLink.tsx` (nome, tamanho, ícone, estado de carregando) com teste
- [x] BE: `@Path("/files")`, `Content-Type` e `Content-Disposition` no `FileResource`, com `ServeFileUseCase` devolvendo `StoredFile`
- [x] BE: 404 para `fileKey` inexistente (exceção de domínio + mapper) e `Content-Disposition` em `cors.exposed-headers`, com teste de integração
- [x] `StudentTaskListPage` renderizando `task.attachments` com `AttachmentLink`, com teste
- [x] `EvaluationDialog` e `SubmissionListDrawer` com os anexos baixáveis, com testes
- [x] `ContentCard` usando `downloadFile` no arquivo e mantendo o link externo, com teste
- [x] Atualizar `API_CONTRACT.md` e a spec `file-storage`; rodar lint, type-check e as duas suítes
- [x] Validar no navegador: anexo de tarefa (aluno), de submissão (professor) e de conteúdo (disciplina)
