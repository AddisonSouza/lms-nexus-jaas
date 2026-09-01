# LMS Nexus — Contrato de API

> **Base URL:** `http://localhost:8080`  
> **Swagger UI:** `http://localhost:8080/api/swagger-ui`  
> **Versão do documento:** Junho 2026

---

## Conceito do Sistema

**LMS Nexus** é uma plataforma de gestão de aprendizado (LMS) **multi-tenant**, desenvolvida como TCC. Inspirada no Google Classroom, organiza turmas, disciplinas, tarefas e avaliações dentro de **Organizações** educacionais isoladas.

Cada Organização tem seus próprios membros com papéis distintos. A separação de dados é garantida pelo `organization_id` extraído do JWT — nunca enviado no body da requisição.

### Papéis

| Papel | Descrição |
|---|---|
| `ADMIN_ORG` | Criado ao fundar a organização. Gerencia tudo. |
| `GESTOR` | Convidado pelo admin. Gerencia turmas específicas. |
| `PROFESSOR` | Cria tarefas, avalia, publica avisos. |
| `ALUNO` | Consome conteúdo, envia tarefas, ingressa via código. |

### Autenticação

Todas as rotas protegidas exigem `Authorization: Bearer <access_token>`.

| Token | Armazenamento | TTL |
|---|---|---|
| Access Token (JWT RS256) | Memória (`authStore`) | 15 min |
| Refresh Token (UUID) | `httpOnly` cookie + Redis | 7 dias |

**Claims do JWT:** `sub` (userId) · `org` (organizationId) · `groups` (roles)

---

## Status de Implementação

| Status | Significado |
|---|---|
| ✅ Implementado | RF concluído e mergeado |
| 🔍 Em revisão | PR aberto, implementação concluída |
| 📋 Proposto | Especificado, aguarda implementação |
| 🔮 Futuro | Fora do MVP, arquitetura preparada |

---

## Módulo: `identity` — Autenticação

### RF-01 — Cadastro de Usuário ✅

**`POST /auth/register`** · Público

```json
{
  "fullName": "string (obrigatório, máx 150)",
  "email": "string (obrigatório, formato e-mail)",
  "password": "string (obrigatório, mín 8 chars)"
}
```

| Código | Descrição |
|---|---|
| `201` | Usuário criado com status `PENDING_CONFIRMATION`. E-mail de confirmação enviado. |
| `409` | E-mail já cadastrado. |
| `400` | Dados de validação inválidos. |

---

### RF-02 — Autenticação ✅

**`POST /auth/login`** · Público

```json
{ "email": "string", "password": "string" }
```

| Código | Descrição |
|---|---|
| `200` | `{ accessToken, tokenType: "Bearer", expiresIn: 900 }` · Refresh Token no cookie `refresh_token`. |
| `401` | Credenciais inválidas. |
| `403` | Conta não confirmada. |

---

**`POST /auth/logout`** · Autenticado

Cookie `refresh_token` no request. Invalida o Refresh Token no Redis.

| Código | Descrição |
|---|---|
| `204` | Token invalidado. |

---

**`POST /auth/refresh`** · Público (cookie)

Cookie `refresh_token` no request. Rotaciona o par Access + Refresh Token.

| Código | Descrição |
|---|---|
| `200` | Novo `accessToken` + novo cookie `refresh_token`. |
| `401` | Token inválido ou expirado. |

---

### RF-03 — Recuperação de Senha ✅

**`POST /auth/forgot-password`** · Público

```json
{ "email": "string" }
```

Sempre retorna `204` (não revela se o e-mail existe). Envia link com token de 1h.

---

**`POST /auth/reset-password`** · Público

```json
{ "token": "string", "newPassword": "string (mín 8 chars)" }
```

| Código | Descrição |
|---|---|
| `204` | Senha redefinida. Todos os Refresh Tokens do usuário invalidados. |
| `400` | Token inválido, expirado ou já utilizado. |

---

### RF-04 — Confirmação de E-mail ✅

**`GET /auth/confirm-email?token={token}`** · Público

| Código | Descrição |
|---|---|
| `204` | Status do usuário alterado para `ACTIVE`. |
| `400` | Token inválido ou expirado. |

---

**`POST /auth/resend-confirmation`** · Público

```json
{ "email": "string" }
```

Reenvia e-mail de confirmação. Sempre retorna `200`.

---

## Módulo: `organization` — Organizações

### RF-05 — Criação de Organização ✅

**`POST /organizations`** · `ADMIN_ORG | GESTOR | PROFESSOR | ALUNO` (qualquer autenticado)

```json
{
  "name": "string (obrigatório, 2–100 chars)",
  "description": "string (opcional, máx 500)"
}
```

| Código | Descrição |
|---|---|
| `201` | Organização criada. Criador vinculado como `ADMIN_ORG`. |
| `400` | Dados inválidos. |

---

**`GET /organizations`** · Autenticado

Lista as organizações do usuário autenticado (pelo `sub` do JWT), com o papel em
cada uma, ordenadas por nome. Vínculos e organizações removidos (soft delete)
não aparecem.

```json
[
  { "id": "uuid", "name": "string", "role": "ADMIN_ORG | GESTOR | PROFESSOR | ALUNO" }
]
```

| Código | Descrição |
|---|---|
| `200` | Lista das organizações do usuário. Array vazio se ele não pertence a nenhuma. |
| `401` | Não autenticado. |

---

### RF-06 — Gestão de Membros e Convites ✅

**`POST /organizations/{id}/invitations`** · `ADMIN_ORG`

```json
{
  "email": "string (formato e-mail)",
  "role": "GESTOR | PROFESSOR | ALUNO"
}
```

| Código | Descrição |
|---|---|
| `201` | Convite criado. E-mail enviado ao convidado. Token válido por 7 dias. |
| `409` | Usuário já é membro. |

---

**`GET /invitations/{token}`** · Público

Retorna informações do convite (organização, papel, convidante) para preview antes de aceitar.

| Código | Descrição |
|---|---|
| `200` | Dados do convite. |
| `404` | Token inválido ou expirado. |

---

**`GET /invitations/pending`** · Autenticado

Convites pendentes e não expirados endereçados ao **e-mail do usuário
autenticado** (o `sub` do JWT resolve o e-mail; a comparação ignora maiúsculas),
do mais recente para o mais antigo. É o que permite ao convidado chegar ao aceite
depois de se cadastrar, mesmo tendo confirmado o e-mail em outro navegador.
Convite cuja organização foi removida não aparece.

```json
[
  {
    "token": "uuid",
    "organizationId": "uuid",
    "organizationName": "string",
    "role": "ADMIN_ORG | GESTOR | PROFESSOR | ALUNO",
    "expiresAt": "2026-09-06T10:00:00Z"
  }
]
```

| Código | Descrição |
|---|---|
| `200` | Convites pendentes. Array vazio se não houver. |
| `401` | Não autenticado. |

---

**`POST /invitations/{token}/accept`** · Autenticado

Aceita o convite. Cria vínculo em `organization_members`.

O convite vale para o e-mail a que foi endereçado: o link é secreto, mas não é
uma credencial. O e-mail é comparado sem diferenciar maiúsculas de minúsculas.

| Código | Descrição |
|---|---|
| `204` | Membro adicionado com o papel do convite. |
| `401` | Não autenticado. |
| `403` | Convite endereçado a outro e-mail (`INVITATION_NOT_FOR_THIS_USER`). |
| `404` | Convite não encontrado (`INVITATION_NOT_FOUND`). |
| `409` | Convite já utilizado (`INVITATION_ALREADY_USED`) ou usuário já é membro (`ALREADY_A_MEMBER`). |
| `410` | Convite expirado (`INVITATION_EXPIRED`). |

---

**`GET /organizations/{id}/members`** · `ADMIN_ORG`

Lista os membros ativos da organização, ordenados por nome. Nome e e-mail vêm do
módulo `identity`. `owner` marca o criador da organização, que não pode ser
removido nem ter o papel alterado. Vínculos removidos (soft delete) não aparecem.

```json
[
  {
    "id": "uuid do vínculo",
    "userId": "uuid",
    "name": "string",
    "email": "string",
    "role": "ADMIN_ORG | GESTOR | PROFESSOR | ALUNO",
    "joinedAt": "2026-08-30T10:00:00",
    "owner": true
  }
]
```

| Código | Descrição |
|---|---|
| `200` | Membros da organização. |
| `401` | Não autenticado. |
| `403` | Não é `ADMIN_ORG` desta organização (o `{id}` precisa bater com o claim `org`). |

---

**`PATCH /organizations/{id}/members/{userId}`** · `ADMIN_ORG`

Altera o papel de um membro.

```json
{
  "role": "GESTOR | PROFESSOR | ALUNO"
}
```

O papel novo vale na requisição seguinte. O papel viaja no JWT e o token do
membro não é reemitido aqui, então os access tokens dele emitidos antes desta
chamada passam a responder `401 SESSION_STALE` — o front renova em silêncio e
refaz a requisição, e a renovação relê o papel do banco. O refresh token não é
tocado: ninguém é deslogado.

| Código | Descrição |
|---|---|
| `204` | Papel alterado. |
| `401` | Não autenticado. |
| `403` | Sem permissão, ou tentativa de alterar o papel do criador (`CANNOT_CHANGE_OWNER_ROLE`). |
| `404` | Membro não encontrado nesta organização (`MEMBER_NOT_FOUND`). |
| `422` | `ADMIN_ORG` não é atribuível a um membro (`ROLE_NOT_ASSIGNABLE`). |

---

**`DELETE /organizations/{id}/members/{userId}`** · `ADMIN_ORG`

Remove membro da organização (soft delete). Vale na requisição seguinte: os
access tokens do removido emitidos antes desta chamada passam a responder
`401 SESSION_STALE`.

| Código | Descrição |
|---|---|
| `204` | Membro removido. |
| `403` | Sem permissão, ou tentativa de remover o criador (`CANNOT_REMOVE_OWNER`). |
| `404` | Membro não encontrado nesta organização (`MEMBER_NOT_FOUND`). |

---

## Módulo: `classroom` — Turmas

### RF-07 — Gestão de Turmas ✅

**`GET /classrooms`** · `ADMIN_ORG | GESTOR | PROFESSOR | ALUNO`

Lista turmas da organização filtradas pelo papel do usuário (aluno vê só as turmas que participa).

| Código | Descrição |
|---|---|
| `200` | `[ { id, name, description, academicPeriod, status, inviteCode } ]` |

---

**`GET /classrooms/{id}`** · `ADMIN_ORG | GESTOR | PROFESSOR | ALUNO`

| Código | Descrição |
|---|---|
| `200` | Detalhes da turma. |
| `404` | Turma não encontrada ou fora da organização. |

---

**`POST /classrooms`** · `ADMIN_ORG | GESTOR`

```json
{
  "name": "string (obrigatório, máx 255)",
  "description": "string (opcional, máx 2000)",
  "academicPeriod": "string (obrigatório, máx 100)"
}
```

| Código | Descrição |
|---|---|
| `201` | Turma criada com `inviteCode` de 6 chars alfanuméricos. |

---

**`PUT /classrooms/{id}`** · `ADMIN_ORG | GESTOR`

```json
{
  "name": "string (opcional, máx 255)",
  "description": "string (opcional, máx 2000)",
  "academicPeriod": "string (opcional, máx 100)",
  "status": "ACTIVE | ARCHIVED"
}
```

| Código | Descrição |
|---|---|
| `200` | Turma atualizada. |
| `404` | Turma não encontrada. |

---

**`DELETE /classrooms/{id}`** · `ADMIN_ORG | GESTOR`

Soft delete (preenche `deleted_at`).

| Código | Descrição |
|---|---|
| `204` | Turma excluída. |
| `404` | Turma não encontrada. |

---

**`GET /classrooms/{id}/members`** · `ADMIN_ORG | GESTOR | PROFESSOR | ALUNO`

| Código | Descrição |
|---|---|
| `200` | `[ { userId, fullName, email, role } ]` |

---

**`POST /classrooms/{id}/members`** · `ADMIN_ORG | GESTOR`

```json
{
  "userId": "string (obrigatório)",
  "role": "PROFESSOR | ALUNO"
}
```

| Código | Descrição |
|---|---|
| `201` | Membro adicionado. Idempotente: reenvio não cria duplicata. |
| `422` | Usuário não pertence à organização ou turma arquivada. |

---

**`DELETE /classrooms/{id}/members/{userId}`** · `ADMIN_ORG | GESTOR`

| Código | Descrição |
|---|---|
| `204` | Membro removido da turma. |
| `404` | Membro não encontrado. |

---

### RF-08 — Ingresso via Código 🔍

**`POST /classrooms/join`** · `ADMIN_ORG | GESTOR | PROFESSOR | ALUNO`

```json
{ "inviteCode": "string (exatamente 6 chars)" }
```

| Código | Descrição |
|---|---|
| `201` | Ingresso realizado. `role=ALUNO` definido automaticamente. |
| `200` | Usuário já era membro (idempotente). |
| `404` | Código inválido **ou de turma de outra organização**. |
| `422` | Turma arquivada — ingresso bloqueado. |

O código só resolve dentro da organização do JWT: um código válido em outra
organização responde `404 INVALID_INVITE_CODE`, sem revelar que a turma existe.
A resposta de `200`/`201` traz `inviteCode: null` — quem entra pelo código entra
como `ALUNO`, e o `ALUNO` nunca recebe o código de volta.

---

## Módulo: `curriculum` — Disciplinas e Conteúdo

### RF-09 — Gestão de Disciplinas 📋

**`POST /subjects`** · `ADMIN_ORG | GESTOR`

```json
{
  "name": "string (obrigatório)",
  "code": "string (opcional, ex: MAT101)",
  "description": "string (opcional)",
  "workload": "integer (opcional, horas)"
}
```

| Código | Descrição |
|---|---|
| `201` | Disciplina criada na organização. |

---

**`POST /subjects/{id}/classrooms`** · `ADMIN_ORG | GESTOR`

Vincula disciplina a uma turma.

```json
{ "classroomId": "string" }
```

---

**`POST /subjects/{id}/teachers`** · `ADMIN_ORG | GESTOR`

Atribui professor à disciplina dentro de uma turma.

```json
{ "userId": "string", "classroomId": "string" }
```

---

### RF-10 — Conteúdo Complementar 📋

**`POST /subjects/{id}/contents`** · `PROFESSOR`

```json
{
  "title": "string",
  "type": "VIDEO | DOCUMENTO | LINK | ARQUIVO",
  "topicId": "string",
  "url": "string (para LINK/VIDEO externo)",
  "file": "multipart (para upload)"
}
```

---

**`GET /subjects/{id}/contents`** · `PROFESSOR | ALUNO`

Retorna conteúdos agrupados por tópico, em ordem definida pelo professor.

---

## Módulo: `assessment` — Tarefas e Avaliações

### RF-11 — Criação de Tarefas 📋

**`POST /tasks`** · `PROFESSOR`

```json
{
  "title": "string",
  "description": "string (texto rico)",
  "dueDate": "ISO-8601 datetime",
  "maxScore": "number (opcional)",
  "status": "DRAFT | PUBLISHED",
  "subjectId": "string",
  "classroomId": "string"
}
```

Ao publicar (`PUBLISHED`), publica `TaskCreatedEvent` → notificações para alunos.

**Ciclo de vida:** `DRAFT` → `PUBLISHED` → `CLOSED` → `GRADED`

---

### RF-12 — Envio de Resposta pelo Aluno 📋

**`POST /tasks/{id}/submissions`** · `ALUNO`

```json
{
  "textResponse": "string (opcional)",
  "files": "multipart (opcional, múltiplos)"
}
```

| Código | Descrição |
|---|---|
| `201` | Submissão criada. `TaskSubmittedEvent` publicado. |
| `422` | Prazo expirado e submissão não permitida. |

---

**`PUT /tasks/{id}/submissions/{submissionId}`** · `ALUNO`

Edita submissão antes do prazo ou avaliação.

---

### RF-13 — Avaliação de Tarefas 📋

**`PATCH /submissions/{id}/evaluation`** · `PROFESSOR`

```json
{
  "score": "number (opcional, se maxScore definido)",
  "feedback": "string"
}
```

| Código | Descrição |
|---|---|
| `200` | Submissão avaliada. `SubmissionEvaluatedEvent` publicado → notificação ao aluno. |

---

### RF-14 — Visualização de Notas e Feedback 📋

**`GET /classrooms/{id}/my-grades`** · `ALUNO`

Lista todas as tarefas da turma com status da submissão, nota e feedback do aluno autenticado.

---

**`GET /submissions/{id}/feedback`** · `ALUNO`

Detalha nota e feedback de uma submissão específica.

---

## Módulo: `communication` — Comunicação

### RF-15 — Mural de Avisos 📋

**`POST /classrooms/{id}/announcements`** · `PROFESSOR`

```json
{
  "content": "string (texto rico)",
  "attachments": "[ { type: LINK|ARQUIVO, url, title } ]"
}
```

Publica `AnnouncementPostedEvent` → notificações in-app para todos os alunos.

---

**`GET /classrooms/{id}/announcements`** · `PROFESSOR | ALUNO`

Retorna feed em ordem cronológica decrescente.

---

### RF-16 — Notificações In-App 📋

**`GET /notifications`** · Autenticado

Retorna lista de notificações do usuário com contador de não lidas (Redis).

---

**`PATCH /notifications/{id}/read`** · Autenticado

Marca notificação como lida.

---

**`PATCH /notifications/read-all`** · Autenticado

Marca todas as notificações do usuário como lidas (batch).

**Eventos que geram notificações:**

| Evento | Destinatário |
|---|---|
| `TaskCreatedEvent` | Alunos da turma |
| `TaskSubmittedEvent` | Professor responsável |
| `SubmissionEvaluatedEvent` | Aluno que submeteu |
| `AnnouncementPostedEvent` | Alunos da turma |
| `InvitationCreatedEvent` | Usuário convidado |

---

## Módulo: `reporting` — Dashboards

### RF-17 — Dashboard do Administrador 📋

**`GET /organizations/{id}/dashboard`** · `ADMIN_ORG`

Métricas: turmas ativas/arquivadas, membros por papel, tarefas criadas/avaliadas, taxa de entrega, últimas atividades.

---

**`GET /organizations/{id}/reports/pdf`** · `ADMIN_ORG`

Exporta relatório da organização em PDF.

---

### RF-18 — Dashboard do Gestor 📋

**`GET /classrooms/{id}/dashboard`** · `GESTOR`

Métricas: turmas gerenciadas, alunos com pendências/atrasos, comparativo entre turmas.

---

### RF-19 — Dashboard do Professor 📋

**`GET /subjects/{id}/dashboard`** · `PROFESSOR`

Métricas: submissões pendentes de avaliação, distribuição de notas, alunos sem entrega, média por aluno.

---

### RF-20 — Dashboard do Aluno 📋

**`GET /students/me/dashboard`** · `ALUNO`

Métricas: próximas tarefas por urgência, tarefas entregues vs pendentes, últimas notas e feedbacks, média por disciplina.

---

## Módulo: `gamification` — Gamificação 🔮

> Fora do MVP. Módulo isolado, ativado sem alteração no núcleo.

### RF-21 — Sistema de Pontos e Níveis

`gamification.AwardPointsUseCase` — consome `TaskSubmittedEvent`, `ContentAccessedEvent`.

Pontuação configurável por organização: entrega no prazo (+100pts), entrega atrasada (+50pts), nota máxima (+50pts bônus), acesso a material (+10pts, limite 50/dia).

---

### RF-22 — Badges e Conquistas

`gamification.CheckBadgeCriteriaUseCase` — concessão automática por critérios (ex: 10 entregas consecutivas no prazo).

---

### RF-23 — Ranking entre Alunos

`gamification.ClassroomRankingQuery` — ranking por turma ou organização. Professor pode tornar anônimo.

---

## Módulo: `ai` — Inteligência Artificial 🔮

> Implementado como `AIPort` (Adapter isolado). Troca de provedor (OpenAI, Anthropic etc.) sem alterar Use Cases.

### RF-24 — Correção Automática por IA

`ai.AutoEvaluateSubmissionUseCase` — sugestão de nota e justificativa para tarefas dissertativas. Professor aceita, edita ou rejeita.

---

### RF-25 — Geração de Exercícios por IA

`ai.GenerateExercisesUseCase` — professor informa tópico e nível de dificuldade. Sistema gera exercícios via LLM para revisão e publicação.

---

### RF-26 — Assistente de Estudos para Aluno

`ai.StudyAssistantUseCase` + `GenerateStudyPlanUseCase` — chat contextualizado com conteúdo da disciplina, plano de estudos personalizado e exercícios extras.

---

## Convenções Globais

### Erros

```json
{ "code": "BUSINESS_ERROR_CODE", "message": "Descrição legível" }
```

Todos os erros seguem `ProblemDetails` (RFC 7807).

### Sessão obsoleta

Mudar o vínculo de um usuário com a organização — papel alterado, membro
removido — marca as sessões dele como obsoletas. A partir daí, **qualquer**
endpoint autenticado responde `401 SESSION_STALE` ao access token emitido antes
da mudança, até que o token seja renovado. O refresh token não é tocado: o front
renova em silêncio, refaz a requisição e a renovação relê o papel do banco —
ninguém é deslogado.

### Paginação

Endpoints de listagem (RF-09+) seguem o padrão:

```
GET /resource?page=0&size=20&sort=createdAt,desc
```

Resposta: `{ content: [], totalElements, totalPages, number, size }`

### Soft Delete

Todos os recursos usam `deleted_at TIMESTAMP NULL`. Queries filtram `WHERE deleted_at IS NULL` por padrão.

### Multi-tenant

`organization_id` **nunca** vem do request body. É sempre extraído do claim `org` do JWT.

---

## Cobertura por RF

| RF | Módulo | Título | Status | Endpoints |
|---|---|---|---|---|
| RF-01 | identity | Cadastro de Usuário | ✅ | `POST /auth/register` |
| RF-02 | identity | Autenticação | ✅ | `POST /auth/login` · `POST /auth/logout` · `POST /auth/refresh` |
| RF-03 | identity | Recuperação de Senha | ✅ | `POST /auth/forgot-password` · `POST /auth/reset-password` |
| RF-04 | identity | Confirmação de E-mail | ✅ | `GET /auth/confirm-email` · `POST /auth/resend-confirmation` |
| RF-05 | organization | Criação de Organização | ✅ | `POST /organizations` · `GET /organizations` |
| RF-06 | organization | Gestão de Membros | ✅ | `POST /organizations/{id}/invitations` · `GET /invitations/{token}` · `GET /invitations/pending` · `POST /invitations/{token}/accept` · `GET /organizations/{id}/members` · `PATCH /organizations/{id}/members/{userId}` · `DELETE /organizations/{id}/members/{userId}` |
| RF-07 | classroom | Gestão de Turmas | ✅ | `GET /classrooms` · `GET /classrooms/{id}` · `POST /classrooms` · `PUT /classrooms/{id}` · `DELETE /classrooms/{id}` · `GET /classrooms/{id}/members` · `POST /classrooms/{id}/members` · `DELETE /classrooms/{id}/members/{userId}` |
| RF-08 | classroom | Ingresso via Código | 🔍 | `POST /classrooms/join` |
| RF-09 | curriculum | Gestão de Disciplinas | 📋 | `POST /subjects` · `POST /subjects/{id}/classrooms` · `POST /subjects/{id}/teachers` |
| RF-10 | curriculum | Conteúdo Complementar | 📋 | `POST /subjects/{id}/contents` · `GET /subjects/{id}/contents` |
| RF-11 | assessment | Criação de Tarefas | 📋 | `POST /tasks` |
| RF-12 | assessment | Envio de Resposta | 📋 | `POST /tasks/{id}/submissions` · `PUT /tasks/{id}/submissions/{submissionId}` |
| RF-13 | assessment | Avaliação de Tarefas | 📋 | `PATCH /submissions/{id}/evaluation` |
| RF-14 | assessment | Notas e Feedback | 📋 | `GET /classrooms/{id}/my-grades` · `GET /submissions/{id}/feedback` |
| RF-15 | communication | Mural de Avisos | 📋 | `POST /classrooms/{id}/announcements` · `GET /classrooms/{id}/announcements` |
| RF-16 | communication | Notificações In-App | 📋 | `GET /notifications` · `PATCH /notifications/{id}/read` · `PATCH /notifications/read-all` |
| RF-17 | reporting | Dashboard Admin | 📋 | `GET /organizations/{id}/dashboard` · `GET /organizations/{id}/reports/pdf` |
| RF-18 | reporting | Dashboard Gestor | 📋 | `GET /classrooms/{id}/dashboard` |
| RF-19 | reporting | Dashboard Professor | 📋 | `GET /subjects/{id}/dashboard` |
| RF-20 | reporting | Dashboard Aluno | 📋 | `GET /students/me/dashboard` |
| RF-21 | gamification | Pontos e Níveis | 🔮 | — via Domain Events |
| RF-22 | gamification | Badges | 🔮 | — via Domain Events |
| RF-23 | gamification | Ranking | 🔮 | `GET /classrooms/{id}/ranking` |
| RF-24 | ai | Correção por IA | 🔮 | — extensão de `PATCH /submissions/{id}/evaluation` |
| RF-25 | ai | Geração de Exercícios | 🔮 | `POST /subjects/{id}/exercises/generate` |
| RF-26 | ai | Assistente de Estudos | 🔮 | `POST /subjects/{id}/study-assistant/chat` |
