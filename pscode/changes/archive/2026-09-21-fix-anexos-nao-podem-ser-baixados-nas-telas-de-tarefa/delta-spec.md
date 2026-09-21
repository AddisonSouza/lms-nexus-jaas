# fix: anexos não podem ser baixados nas telas de tarefa — Delta

## Added

- **Download autenticado (`apps/web/src/lib/download.ts`).** `downloadFile(fileKey,
  originalName)` busca o arquivo como blob pelo cliente `api` — que anexa o JWT e
  renova a sessão no 401 —, cria um object URL, dispara um `<a download>`
  sintético e revoga o URL depois de 60s. Revogar na hora aborta o download: o
  navegador ainda está lendo quando o clique retorna.
  `fileNameFromKey(fileKey)` extrai o nome da chave (`{contexto}/{ano}/{mês}/{uuid}-{nome}`)
  para quem tem a chave mas não guardou o nome.
- **`components/shared/AttachmentLink`** — nome, tamanho legível (`2,0 KB`), ícone,
  estado de carregando e mensagem própria de falha. É `<button>`, não `<a href>`:
  o link direto não leva token.
- **`RetrievedFile`** no domínio do storage: conteúdo + `StoredFile`.
  `StoragePort.retrieve` passa a devolvê-lo, para a resposta HTTP ter tipo e nome
  sem uma segunda ida ao storage.
- **`FileNotFoundException`** (`HttpMappable`, 404, `FILE_NOT_FOUND`).
- **Nome original como metadado do objeto** (`original-name`) no upload. A chave
  só guarda a forma sanitizada (`prova final.pdf` → `prova_final.pdf`), então o
  `Content-Disposition` sairia com o nome errado. Arquivos gravados antes disso
  caem no nome embutido na chave.
- Testes: `download` (7), `AttachmentLink` (5), `ContentCard` (3, novo),
  anexos no `StudentTaskListPage` (2) e no `SubmissionListDrawer` (2), e
  `FileResourceIT` (4) — o primeiro teste do módulo `storage`.

## Changed

- **`GET /api/files/{fileKey}` → `GET /files/{fileKey}`.** Era a única resource
  com prefixo `/api`. O único consumidor era o `ContentCard`, atualizado junto —
  o prop `apiBaseUrl` sai de `ContentCard`, `TopicList` e `SubjectDetailPage`.
- **A resposta passa a carregar `Content-Type` real e `Content-Disposition:
  attachment`**, com `filename` ASCII e `filename*` RFC 5987. `Content-Disposition`
  entrou em `quarkus.http.cors.exposed-headers`; sem isso o navegador esconde o
  cabeçalho do JavaScript.
- **Chave inexistente responde 404 `FILE_NOT_FOUND`.** Antes o
  `NoSuchKeyException` do SDK subia cru e virava 500: anexo apagado parecia falha
  do servidor.
- **`StudentTaskListPage`** renderiza `task.attachments` no card. Os dados já
  vinham no `my-grades` e nunca eram desenhados — o aluno via a tarefa e não
  tinha como chegar ao arquivo.
- **`SubmissionListDrawer`** troca `"N anexo(s)"` pela lista baixável;
  **`EvaluationDialog`** troca o nome estático pelo `AttachmentLink`.
- **`ContentCard`** baixa o arquivo pelo `downloadFile`; `externalUrl` continua
  `<a target="_blank">`. Conteúdo sem arquivo e sem link não oferece nada.

## Removed

- Nada.

## Conhecido, fora deste card

- **`GET /files/{fileKey}` não valida organização.** Quem tem a chave baixa o
  arquivo, de qualquer organização — o `fileKey` não carrega `organization_id`.
  Gap registrado no `API_CONTRACT.md` e na spec `file-storage`; card próprio.
- **A spec afirmava que essa validação existia.** O texto foi corrigido para
  descrever o comportamento real.
- Upload e pré-visualização no navegador; nome do aluno no `SubmissionListDrawer`;
  anexos de aviso (`AnnouncementCard`), que têm o mesmo defeito e viram card.
- O metadado `original-name` só vale para uploads daqui em diante; a validação no
  navegador exercitou o fallback, não o caminho novo.
