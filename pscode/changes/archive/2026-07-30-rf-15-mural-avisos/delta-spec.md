# rf-15-mural-avisos — Delta

## Added
- Capability `classroom-announcements`: professor vinculado à turma publica aviso com conteúdo em texto rico (obrigatório) e anexos opcionais (arquivo via `StoragePort` e/ou link externo).
- `GET /classrooms/{id}/announcements` retorna o feed da turma em ordem cronológica decrescente; aluno matriculado tem acesso somente de leitura.
- `POST /classrooms/{id}/announcements` publica o aviso e dispara `AnnouncementPostedEvent`, que gera notificação in-app para todos os alunos da turma.
- Professor autor pode editar e excluir os próprios avisos (`EditAnnouncementService`, `DeleteAnnouncementService`), com soft delete via `deleted_at`.
- Frontend: feed de avisos em `features/communication`.

## Changed
- `in-app-notifications` passa a ter `AnnouncementPostedEvent` como nova origem de notificação, além dos eventos já existentes.
