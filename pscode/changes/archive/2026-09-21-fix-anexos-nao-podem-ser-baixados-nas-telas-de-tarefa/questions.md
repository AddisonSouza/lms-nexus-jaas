# Questions

- [x] **Como o front baixa o arquivo autenticado?** Blob via axios
  (`responseType: 'blob'` + `createObjectURL` + `<a download>`), reusando o
  interceptor de token e o refresh 401. Mesmo padrão do export de PDF.
- [x] **Escopo do back-end?** `Content-Type` real, `Content-Disposition` com o
  nome original e 404 para chave inexistente. Sem validação de organização.
- [x] **A rota muda?** Sim: `/api/files/{fileKey}` → `/files/{fileKey}`,
  alinhando com as demais resources.
- [x] **`ContentCard` entra?** Sim — mesmo defeito (`<a href>` sem token → 401).
- [x] **Onde o aluno vê os anexos?** Na linha do card de `StudentTaskListPage`.

## Em aberto (fora desta change)
- [ ] `GET /files/{fileKey}` não valida se o arquivo é da organização do JWT —
  a spec `file-storage` exige. O `fileKey` não carrega `organization_id` e vive
  em 4 tabelas de módulos diferentes. Card próprio.
- [ ] Nome do aluno no `SubmissionListDrawer` (item `[feat]
  lista-submissoes-com-aluno-e-anexos` da bateria E2E).
