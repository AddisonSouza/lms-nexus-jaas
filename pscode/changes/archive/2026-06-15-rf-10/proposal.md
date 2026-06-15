## Why

RF-10 entrega a espinha dorsal pedagógica do módulo `curriculum`: professores precisam publicar materiais (vídeos, PDFs, links, arquivos) organizados hierarquicamente por tópico dentro de cada disciplina — diferencial em relação ao Google Classroom, que não possui essa organização. Sem isso, as disciplinas (RF-09) existem mas ficam vazias de conteúdo.

## What Changes

- **[BE + FE]** Novo aggregate `Topic` no módulo `curriculum` com CRUD próprio (`/subjects/{id}/topics`): criar, reordenar, excluir tópicos dentro de uma disciplina
- **[BE + FE]** Novo aggregate `SubjectContent` no módulo `curriculum` com CRUD (`/subjects/{id}/contents`): criar, editar, excluir materiais associados a tópicos; tipos `VIDEO`, `DOCUMENTO`, `LINK`, `ARQUIVO`
- **[BE]** Novo módulo `storage`: `StoragePort` (interface em `domain/port/out/`) + `LocalStorageAdapter` (implementação local para dev); aceita pdf, mp4, webm, doc, docx, zip, imagens
- **[FE]** Página de detalhe de disciplina com painel de tópicos: professor gerencia tópicos e materiais; aluno visualiza conteúdo agrupado por tópico em ordem definida pelo professor
- **[INFRA]** Migrations Flyway: `subject_topics` e `subject_contents`

## Capabilities

### New Capabilities

- `topic-management`: CRUD e reordenação de tópicos dentro de uma disciplina (PROFESSOR); leitura por ALUNO com restrição de acesso via matrícula
- `subject-content`: criação, edição e exclusão de materiais por tipo (VIDEO/DOCUMENTO/LINK/ARQUIVO) associados a tópicos; upload de arquivo via `StoragePort`; listagem agrupada por tópico
- `file-storage`: abstração de armazenamento de arquivos — `StoragePort` com implementação `LocalStorageAdapter` para desenvolvimento; endpoint de serving `/api/files/{fileKey}`

### Modified Capabilities

*(nenhuma — RF-10 acrescenta capacidades ao módulo curriculum sem alterar comportamentos existentes)*

## Impact

- **Backend:** módulo `curriculum` (novos use cases, domain models, JPA entities, endpoints REST) + módulo `storage` (novo)
- **Frontend:** `features/curriculum` — nova sub-feature de tópicos e materiais; `features/curriculum/types.ts` estendido
- **Banco de dados:** duas novas tabelas (`subject_topics`, `subject_contents`) com migrations V013 e V014
- **Non-goals:** adapter S3/MinIO, streaming de vídeo nativo, busca full-text em conteúdo, RF-11+
