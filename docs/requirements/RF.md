# LMS — Especificação de Requisitos Funcionais

> **Versão:** 1.0.0 — Maio 2026  
> **Total:** 26 requisitos — 20 MVP + 6 Evolução Futura

---

## Convenção de Escopo

| Badge | Significado |
|---|---|
| `[MVP]` | Entregável na primeira versão do sistema |
| `[FUTURO]` | Evolução planejada após o MVP — arquitetura já preparada |

## Papéis do Sistema

| Papel | Descrição |
|---|---|
| `ADMIN_ORG` | Criado automaticamente ao criar uma Organização. Gerencia tudo. |
| `GESTOR` | Convidado pelo ADMIN_ORG. Gerencia turmas específicas. |
| `PROFESSOR` | Convidado. Cria tarefas, avalia, publica avisos. |
| `ALUNO` | Ingresso via link/código ou convite. Consome conteúdo, envia tarefas. |

---

## Visão Geral

| ID | Módulo | Título | Atores | Escopo |
|---|---|---|---|---|
| RF-01 | identity | Cadastro de Usuário | Qualquer | MVP |
| RF-02 | identity | Autenticação (Login / Logout) | Todos | MVP |
| RF-03 | identity | Recuperação de Senha | Todos | MVP |
| RF-04 | identity | Confirmação de E-mail | Todos | MVP |
| RF-05 | organization | Criação de Organização | Usuário autenticado | MVP |
| RF-06 | organization | Gestão de Membros e Convites | ADMIN_ORG | MVP |
| RF-07 | classroom | Gestão de Turmas | ADMIN_ORG, GESTOR | MVP |
| RF-08 | classroom | Ingresso em Turma via Link/Código | ALUNO | MVP |
| RF-09 | curriculum | Gestão de Disciplinas | ADMIN_ORG, GESTOR, PROFESSOR | MVP |
| RF-10 | curriculum | Conteúdo Complementar | PROFESSOR | MVP |
| RF-11 | assessment | Criação de Tarefas | PROFESSOR | MVP |
| RF-12 | assessment | Envio de Resposta pelo Aluno | ALUNO | MVP |
| RF-13 | assessment | Avaliação de Tarefas | PROFESSOR | MVP |
| RF-14 | assessment | Visualização de Notas e Feedback | ALUNO | MVP |
| RF-15 | communication | Mural de Avisos | PROFESSOR, ALUNO | MVP |
| RF-16 | communication | Notificações In-App | Todos | MVP |
| RF-17 | reporting | Dashboard do Administrador | ADMIN_ORG | MVP |
| RF-18 | reporting | Dashboard do Gestor | GESTOR | MVP |
| RF-19 | reporting | Dashboard do Professor | PROFESSOR | MVP |
| RF-20 | reporting | Dashboard do Aluno | ALUNO | MVP |
| RF-21 | gamification | Sistema de Pontos e Níveis | ALUNO | FUTURO |
| RF-22 | gamification | Badges e Conquistas | ALUNO | FUTURO |
| RF-23 | gamification | Ranking entre Alunos | ALUNO, PROFESSOR | FUTURO |
| RF-24 | ai | Correção Automática por IA | PROFESSOR | FUTURO |
| RF-25 | ai | Geração de Exercícios por IA | PROFESSOR | FUTURO |
| RF-26 | ai | Assistente de Estudos para Aluno | ALUNO | FUTURO |

---

# PARTE I — MVP

---

## Módulo: identity — Identidade e Autenticação

---

### RF-01 — Cadastro de Usuário `[MVP]`

**Descrição:** O sistema deve permitir que qualquer pessoa crie uma conta fornecendo nome completo, e-mail e senha. Após o cadastro, um e-mail de confirmação é enviado. A conta só fica ativa após a confirmação.

**Atores:** Usuário não autenticado  
**Módulo BE:** `identity` | `RegisterUserUseCase`  
**Módulo FE:** `features/auth` | `RegisterForm`  
**Endpoint:** `POST /auth/register`  
**Pré-condições:** Nenhuma. Tela pública.  
**Pós-condições:** Usuário criado com status `PENDING_CONFIRMATION`. E-mail de confirmação enviado.

**Fluxo Principal:**
1. Usuário acessa a página de cadastro
2. Preenche nome completo, e-mail e senha
3. Sistema valida: e-mail único, senha com mínimo 8 caracteres
4. Sistema cria usuário com status `PENDING_CONFIRMATION`
5. Sistema dispara e-mail com link de confirmação (token com validade de 24h)
6. Usuário é redirecionado para página informando que deve confirmar o e-mail

**Fluxos Alternativos:**
- E-mail já cadastrado → retorna erro 409 "E-mail já em uso"
- Senha fraca → mensagem de validação exibida antes do envio
- Token de confirmação expirado → sistema oferece opção de reenviar o e-mail

**Regras de Negócio:**
- `RN-03`: Criador de Organização torna-se automaticamente ADMIN_ORG
- `SEC-01`: Senha armazenada com BCrypt, fator mínimo 12
- `DB-MT-04`: Tabela `users` é global; papéis ficam em `organization_members`

**Critérios de Aceite:**
- [ ] Usuário criado com status `PENDING_CONFIRMATION` após cadastro
- [ ] E-mail de confirmação enviado com token de 24h
- [ ] E-mail duplicado retorna 409
- [ ] Senha com menos de 8 caracteres rejeitada com mensagem clara
- [ ] Login bloqueado até confirmação do e-mail

---

### RF-02 — Autenticação (Login / Logout) `[MVP]`

**Descrição:** O sistema deve permitir login com e-mail e senha, retornando Access Token (JWT RS256, 15min) e Refresh Token (7 dias, Redis). Logout invalida o Refresh Token.

**Atores:** Usuário com conta confirmada (`ACTIVE`)  
**Módulo BE:** `identity` | `AuthenticateUseCase`  
**Módulo FE:** `features/auth` | `LoginForm`, `authStore`  
**Endpoints:** `POST /auth/login` | `POST /auth/logout` | `POST /auth/refresh`

**Fluxo Principal — Login:**
1. Usuário informa e-mail e senha
2. Sistema valida credenciais (BCrypt compare)
3. Sistema emite Access Token (JWT RS256, 15min) com claims: `sub`, `org`, `roles`
4. Sistema gera Refresh Token (UUID), armazena no Redis com TTL 7 dias
5. Front-end armazena Access Token em memória (`authStore`) e Refresh Token em httpOnly cookie

**Fluxo — Refresh Token:**
1. Access Token expira; Axios interceptor chama `POST /auth/refresh`
2. Sistema valida Refresh Token no Redis
3. Sistema emite novo par Access Token + Refresh Token (rotação)

**Fluxos Alternativos:**
- Credenciais inválidas → 401, sem indicar qual campo está errado
- Conta não confirmada → 403 com orientação
- 5+ tentativas falhas em 1 minuto por IP → bloqueio de 15 minutos (`SEC-08`)

**Regras de Negócio:** `SEC-02` | `SEC-03` | `SEC-04`

**Critérios de Aceite:**
- [ ] Login com credenciais válidas retorna Access Token + Refresh Token
- [ ] Access Token expira em 15 minutos
- [ ] Refresh Token armazenado no Redis com TTL de 7 dias
- [ ] Logout remove o Refresh Token do Redis
- [ ] Rate limiting bloqueia após 5 tentativas falhas por IP

---

### RF-03 — Recuperação de Senha `[MVP]`

**Descrição:** O sistema deve permitir que o usuário solicite redefinição de senha via e-mail. Link com token de uso único expira em 1 hora.

**Atores:** Usuário com conta ativa  
**Módulo BE:** `identity` | `RequestPasswordResetUseCase`, `ResetPasswordUseCase`  
**Endpoints:** `POST /auth/forgot-password` | `POST /auth/reset-password`

**Fluxo Principal:**
1. Usuário acessa "Esqueci minha senha" e informa o e-mail
2. Sistema verifica se o e-mail existe (sem revelar na resposta)
3. Se existir: gera token UUID, armazena no Redis com TTL 1h, envia e-mail com link
4. Usuário clica no link, informa nova senha (2x para confirmação)
5. Sistema valida token, atualiza senha com BCrypt, invalida token e todos os Refresh Tokens do usuário

**Fluxos Alternativos:**
- Token expirado → mensagem + nova solicitação
- Token já utilizado → rejeita com 400
- Senhas não conferem → validação no front antes do envio

**Critérios de Aceite:**
- [ ] Link de recuperação enviado com token de 1h
- [ ] Token de uso único — segunda tentativa com mesmo token retorna 400
- [ ] Todos os Refresh Tokens do usuário invalidados após reset
- [ ] Resposta da API não revela se o e-mail existe ou não

---

### RF-04 — Confirmação de E-mail `[MVP]`

**Descrição:** Após o cadastro, o usuário recebe um e-mail com link de confirmação. Sem a confirmação, o login é bloqueado.

**Atores:** Usuário recém-cadastrado  
**Módulo BE:** `identity` | `ConfirmEmailUseCase`  
**Endpoint:** `GET /auth/confirm-email?token={token}`

**Fluxo Principal:**
1. Usuário clica no link recebido por e-mail
2. Sistema valida o token (Redis, TTL 24h)
3. Sistema atualiza status de `PENDING_CONFIRMATION` para `ACTIVE`
4. Usuário é redirecionado para login com mensagem de sucesso

**Fluxos Alternativos:**
- Token expirado → página com opção de reenvio
- Conta já confirmada → aceita e redireciona para login sem erro

**Critérios de Aceite:**
- [ ] Status muda para `ACTIVE` após confirmação válida
- [ ] Token expirado exibe opção de reenvio
- [ ] Confirmação idempotente — segunda confirmação não retorna erro

---

## Módulo: organization — Organizações e Membros

---

### RF-05 — Criação de Organização `[MVP]`

**Descrição:** Usuário autenticado pode criar uma Organização educacional. Ao criar, torna-se automaticamente `ADMIN_ORG`. Um usuário pode ser `ADMIN_ORG` de múltiplas organizações.

**Atores:** Usuário autenticado  
**Módulo BE:** `organization` | `CreateOrganizationUseCase`  
**Módulo FE:** `features/organization` | `CreateOrganizationForm`  
**Endpoint:** `POST /organizations`

**Campos:**
- Nome (obrigatório, único por usuário)
- Descrição (opcional)
- Logotipo (opcional, upload via StoragePort)

**Fluxo Principal:**
1. Usuário acessa "Criar Organização" e preenche o formulário
2. Sistema valida campos e cria o registro em `organizations`
3. Sistema cria entrada em `organization_members`: `user_id + organization_id + role=ADMIN_ORG`
4. Sistema publica `OrganizationCreatedEvent`
5. Usuário é redirecionado para o dashboard da nova organização

**Regras de Negócio:** `RN-03` | `DB-MT-01`

**Critérios de Aceite:**
- [ ] Organização criada com `organization_id` único
- [ ] Criador vinculado em `organization_members` com `role=ADMIN_ORG`
- [ ] `OrganizationCreatedEvent` publicado
- [ ] Usuário pode criar múltiplas organizações

---

### RF-06 — Gestão de Membros e Convites `[MVP]`

**Descrição:** `ADMIN_ORG` pode convidar usuários por e-mail definindo o papel (GESTOR, PROFESSOR ou ALUNO). Convite tem validade de 7 dias. Membros podem ter papel alterado ou ser removidos.

**Atores:** `ADMIN_ORG` (convidar, alterar, remover) | Usuário convidado (aceitar)  
**Módulo BE:** `organization` | `InviteMemberUseCase`, `AcceptInviteUseCase`  
**Endpoints:** `POST /organizations/{id}/invitations` | `POST /invitations/{token}/accept` | `DELETE /organizations/{id}/members/{userId}`

**Fluxo — Convidar:**
1. `ADMIN_ORG` informa e-mail e seleciona o papel
2. Sistema verifica se o e-mail já é membro
3. Sistema gera token UUID, armazena em `invitations` com TTL 7 dias
4. Sistema envia e-mail ao convidado com link de aceite

**Fluxo — Aceitar Convite:**
1. Convidado clica no link (se não tiver conta, é redirecionado para cadastro)
2. Sistema valida token (existência, TTL, não utilizado)
3. Sistema cria entrada em `organization_members` com o papel do convite
4. Token marcado como `USED`

**Fluxos Alternativos:**
- E-mail já membro → aviso, sem duplicata
- Token expirado → convite invalidado, ADMIN_ORG reenvia
- Remoção de membro → soft delete em `organization_members`

**Regras de Negócio:** `RN-01` | `RN-03` | `RN-04`

**Critérios de Aceite:**
- [ ] Convite gerado com token de uso único e TTL 7 dias
- [ ] E-mail de convite enviado ao destinatário
- [ ] Aceite cria vínculo em `organization_members` com papel correto
- [ ] Token usado não pode ser reutilizado
- [ ] ADMIN_ORG criador não pode ser removido

---

## Módulo: classroom — Turmas

---

### RF-07 — Gestão de Turmas `[MVP]`

**Descrição:** `ADMIN_ORG` e `GESTOR` podem criar, editar e excluir (soft delete) turmas. Professores e alunos são vinculados. Cada turma pertence a uma organização.

**Atores:** `ADMIN_ORG` (CRUD completo) | `GESTOR` (CRUD nas turmas sob sua gestão)  
**Módulo BE:** `classroom` | `CreateClassroomUseCase`, `UpdateClassroomUseCase`, `DeleteClassroomUseCase`  
**Endpoints:** `POST /classrooms` | `PUT /classrooms/{id}` | `DELETE /classrooms/{id}` | `POST /classrooms/{id}/members`

**Campos:**
- Nome (obrigatório)
- Descrição (opcional)
- Período letivo (obrigatório)
- Status: `ACTIVE` | `ARCHIVED`

**Fluxo Principal — Criação:**
1. Ator preenche dados e confirma
2. Sistema valida papel (extraído do JWT)
3. Turma criada com `organization_id` do JWT
4. Sistema gera código de convite aleatório (6 caracteres alfanuméricos)

**Fluxo — Vincular Membros:**
1. Ator seleciona membros da organização e vincula com papel `PROFESSOR` ou `ALUNO`
2. Sistema cria entradas em `classroom_members`

**Fluxos Alternativos:**
- Exclusão com alunos ativos → alerta + confirmação, soft delete aplicado
- Vincular membro já existente → idempotente

**Critérios de Aceite:**
- [ ] Turma criada com código de convite único de 6 caracteres
- [ ] `organization_id` vem do JWT, nunca do body
- [ ] Soft delete preserva histórico
- [ ] Apenas ADMIN_ORG e GESTOR criam turmas

---

### RF-08 — Ingresso em Turma via Link/Código `[MVP]`

**Descrição:** Alunos ingressam em turmas usando link público ou código de 6 caracteres, sem convite por e-mail.

**Atores:** `ALUNO` (ingresso) | `PROFESSOR`, `GESTOR` (geração do código)  
**Módulo BE:** `classroom` | `JoinClassroomUseCase`  
**Endpoints:** `POST /classrooms/join` | `POST /classrooms/{id}/invite-code/regenerate`

**Fluxo Principal:**
1. Aluno acessa o link ou digita o código
2. Sistema valida o código (existência, turma `ACTIVE`, organização ativa)
3. Sistema verifica se o aluno já é membro (idempotente)
4. Sistema cria entrada em `classroom_members` com `role=ALUNO`
5. Aluno é redirecionado para a turma

**Fluxos Alternativos:**
- Código inválido → mensagem de erro clara
- Turma arquivada → ingresso bloqueado

**Regras de Negócio:** `RN-05`

**Critérios de Aceite:**
- [ ] Ingresso via código sem necessidade de convite por e-mail
- [ ] Código inválido retorna mensagem clara
- [ ] Turma arquivada bloqueia novo ingresso
- [ ] Ingresso idempotente — segundo ingresso não cria duplicata

---

## Módulo: curriculum — Disciplinas e Conteúdo

---

### RF-09 — Gestão de Disciplinas `[MVP]`

**Descrição:** Disciplinas são criadas dentro de uma organização e vinculadas a turmas e professores. Um professor pode lecionar múltiplas disciplinas.

**Atores:** `ADMIN_ORG` (CRUD completo) | `GESTOR` | `PROFESSOR` (visualização e gestão de conteúdo)  
**Módulo BE:** `curriculum` | `CreateSubjectUseCase`, `LinkSubjectToClassroomUseCase`  
**Endpoints:** `POST /subjects` | `POST /subjects/{id}/classrooms` | `POST /subjects/{id}/teachers`

**Campos:**
- Nome (obrigatório)
- Código/sigla (opcional, ex: MAT101)
- Descrição (opcional)
- Carga horária (opcional)

**Fluxo Principal:**
1. ADMIN_ORG/GESTOR cria a disciplina na organização
2. Vincula a disciplina a uma ou mais turmas
3. Atribui um ou mais professores à disciplina dentro da turma

**Regras de Negócio:** `RN-06` | `RN-07`

**Critérios de Aceite:**
- [ ] Disciplina criada com `organization_id` correto
- [ ] Vínculo disciplina-turma registrado em `subject_classrooms`
- [ ] Professor pode ser vinculado a múltiplas disciplinas
- [ ] Aluno só vê disciplinas das turmas às quais pertence

---

### RF-10 — Conteúdo Complementar `[MVP]`

**Descrição:** Professores publicam materiais organizados por disciplina e tópico (videoaulas, PDFs, links, arquivos). Diferencial em relação ao Google Classroom: organização hierárquica por tópico.

**Atores:** `PROFESSOR` (publicar, editar, excluir) | `ALUNO` (visualizar, baixar)  
**Módulo BE:** `curriculum` | `CreateContentUseCase` + `storage (StoragePort)`  
**Endpoints:** `POST /subjects/{id}/contents` | `GET /subjects/{id}/contents`

**Tipos de Conteúdo:**
- `VIDEO`: link externo (YouTube, Vimeo) ou upload de arquivo (mp4, webm)
- `DOCUMENTO`: upload de PDF, DOC, DOCX
- `LINK`: URL externa com título e descrição
- `ARQUIVO`: qualquer arquivo (zip, imagem etc.)

**Organização por Tópico:**
1. Professor cria tópicos dentro da disciplina (ex: "Unidade 1 — Introdução")
2. Cada material é associado a um tópico
3. Front-end exibe materiais agrupados por tópico em ordem definida pelo professor

**Critérios de Aceite:**
- [ ] Conteúdo criado e associado a tópico da disciplina
- [ ] Materiais exibidos agrupados por tópico
- [ ] Upload de arquivo usa `StoragePort` (independente de implementação)
- [ ] Aluno só acessa conteúdo das disciplinas às quais pertence

---

## Módulo: assessment — Tarefas e Avaliações

---

### RF-11 — Criação de Tarefas `[MVP]`

**Descrição:** Professores criam tarefas com título, descrição, prazo e materiais de apoio. A tarefa é vinculada a uma disciplina dentro de uma turma.

**Atores:** `PROFESSOR`  
**Módulo BE:** `assessment` | `CreateTaskUseCase` (publica `TaskCreatedEvent`)  
**Endpoint:** `POST /tasks`  
**Pós-condições:** Tarefa criada. Se `PUBLISHED`, notificação disparada para alunos.

**Campos:**
- Título (obrigatório)
- Descrição/enunciado (obrigatório, texto rico)
- Prazo de entrega (data e hora, obrigatório)
- Pontuação máxima (opcional)
- Status: `DRAFT` | `PUBLISHED`
- Materiais de apoio (arquivos via StoragePort, links externos, vídeos)

**Ciclo de Vida:**
- `DRAFT` → criada, só professor vê
- `PUBLISHED` → visível aos alunos, aceita submissões
- `CLOSED` → prazo expirado, não aceita novas submissões
- `GRADED` → todas as submissões avaliadas

**Critérios de Aceite:**
- [ ] Tarefa `DRAFT` invisível para alunos
- [ ] `TaskCreatedEvent` publicado ao publicar tarefa
- [ ] Prazo obrigatório e com data futura
- [ ] Status evolui corretamente conforme prazo

---

### RF-12 — Envio de Resposta pelo Aluno `[MVP]`

**Descrição:** Alunos visualizam tarefas publicadas e enviam respostas (texto, arquivos ou ambos) até o prazo.

**Atores:** `ALUNO`  
**Módulo BE:** `assessment` | `SubmitTaskUseCase`  
**Endpoints:** `POST /tasks/{id}/submissions` | `PUT /tasks/{id}/submissions/{submissionId}`

**Campos da Submissão:**
- Texto de resposta (opcional, texto rico)
- Arquivos em anexo (upload via StoragePort, múltiplos)
- Status: `SUBMITTED` | `LATE` | `EVALUATED`

**Fluxo Principal:**
1. Aluno acessa a tarefa e clica em "Enviar resposta"
2. Preenche texto e/ou anexa arquivos
3. Sistema valida prazo. Se expirado, bloqueia ou marca como `LATE`
4. Submissão salva. `TaskSubmittedEvent` publicado (notificação ao professor)

**Critérios de Aceite:**
- [ ] Submissão aceita com texto, arquivo ou ambos
- [ ] Prazo expirado bloqueia nova submissão
- [ ] `TaskSubmittedEvent` publicado após envio
- [ ] Aluno pode editar submissão antes do prazo ou avaliação

---

### RF-13 — Avaliação de Tarefas `[MVP]`

**Descrição:** Professores visualizam submissões e atribuem nota e feedback textual a cada uma.

**Atores:** `PROFESSOR`  
**Módulo BE:** `assessment` | `EvaluateSubmissionUseCase`  
**Endpoint:** `PATCH /submissions/{id}/evaluation`  
**Pós-condições:** Status da submissão → `EVALUATED`. `SubmissionEvaluatedEvent` publicado.

**Fluxo Principal:**
1. Professor acessa a tarefa e visualiza lista de submissões
2. Seleciona uma submissão para avaliar
3. Visualiza resposta do aluno (texto + arquivos para download)
4. Atribui nota (se pontuação máxima definida) e escreve feedback
5. Sistema salva avaliação e publica `SubmissionEvaluatedEvent`

**Fluxos Alternativos:**
- Tarefa sem pontuação máxima → campo de nota desabilitado, apenas feedback
- Aluno sem submissão → professor pode registrar ausência/zero manualmente

**Critérios de Aceite:**
- [ ] Nota e feedback salvos na submissão
- [ ] `SubmissionEvaluatedEvent` publicado (gera notificação ao aluno)
- [ ] Tarefa sem pontuação aceita apenas feedback textual
- [ ] Lista de submissões exibe status de cada aluno

---

### RF-14 — Visualização de Notas e Feedback `[MVP]`

**Descrição:** Alunos visualizam notas e feedbacks recebidos. Nota visível apenas após avaliação do professor.

**Atores:** `ALUNO`  
**Módulo BE:** `assessment` | `ListStudentGradesUseCase`  
**Endpoints:** `GET /classrooms/{id}/my-grades` | `GET /submissions/{id}/feedback`

**Informações Exibidas:**
- Lista de tarefas com status de cada submissão
- Nota recebida (se avaliado com pontuação)
- Feedback textual do professor
- Prazo da tarefa e data de envio da resposta
- Indicação se a entrega foi dentro ou fora do prazo

**Critérios de Aceite:**
- [ ] Nota exibida apenas após avaliação do professor
- [ ] Histórico completo de todas as tarefas da turma
- [ ] Status de cada submissão visível (pendente, enviada, avaliada, atrasada)

---

## Módulo: communication — Comunicação

---

### RF-15 — Mural de Avisos `[MVP]`

**Descrição:** Professores publicam avisos na turma exibidos em ordem cronológica decrescente (feed). Alunos apenas visualizam.

**Atores:** `PROFESSOR` (publicar, editar, excluir próprios) | `ALUNO` (visualizar)  
**Módulo BE:** `communication` | `PostAnnouncementUseCase`, `ListAnnouncementsUseCase`  
**Endpoints:** `POST /classrooms/{id}/announcements` | `GET /classrooms/{id}/announcements`

**Campos:**
- Conteúdo (obrigatório, texto rico)
- Anexos (opcional: arquivos via StoragePort ou links)
- Data de publicação (automática)

**Fluxo Principal:**
1. Professor escreve aviso e opcionalmente adiciona anexos
2. Sistema salva e exibe no topo do feed
3. `AnnouncementPostedEvent` publicado → notificação in-app para todos os alunos da turma

**Critérios de Aceite:**
- [ ] Avisos exibidos em ordem cronológica decrescente
- [ ] `AnnouncementPostedEvent` publicado ao criar aviso
- [ ] Alunos não podem publicar avisos
- [ ] Professor pode editar e excluir apenas seus próprios avisos

---

### RF-16 — Notificações In-App `[MVP]`

**Descrição:** O sistema gera notificações in-app para eventos relevantes. Exibidas no sino de notificações. Contador de não lidas no Redis.

**Atores:** Todos os usuários  
**Módulo BE:** `communication` | `CreateNotificationUseCase` (consumidor de Domain Events)  
**Endpoints:** `GET /notifications` | `PATCH /notifications/{id}/read` | `PATCH /notifications/read-all`

**Eventos que Geram Notificações:**

| Evento | Quem Recebe |
|---|---|
| `TaskCreatedEvent` (tarefa publicada) | Todos os alunos da turma |
| `TaskSubmittedEvent` | Professor responsável pela tarefa |
| `SubmissionEvaluatedEvent` | Aluno que submeteu |
| `AnnouncementPostedEvent` | Todos os alunos da turma |
| `InvitationCreatedEvent` | Usuário convidado (por e-mail também) |

**Estrutura da Notificação:**
- Tipo (`TASK_PUBLISHED`, `SUBMISSION_EVALUATED` etc.)
- Título e mensagem curta
- Link de ação (deep link para o recurso)
- Status: `UNREAD` | `READ`
- Data de criação

> **Nota:** Notificações em tempo real (WebSocket/SSE) são evolução futura. No MVP, contador atualizado a cada carregamento e via polling leve (30s).

**Critérios de Aceite:**
- [ ] Notificação criada para cada evento listado acima
- [ ] Contador de não lidas armazenado no Redis
- [ ] Marcar como lida atualiza status individual
- [ ] "Marcar todas como lidas" funciona em batch

---

## Módulo: reporting — Dashboards e Relatórios

---

### RF-17 — Dashboard do Administrador `[MVP]`

**Atores:** `ADMIN_ORG`  
**Módulo BE:** `reporting` | `AdminDashboardQuery`  
**Endpoints:** `GET /organizations/{id}/dashboard` | `GET /organizations/{id}/reports/pdf`

**Métricas:**
- Total de turmas ativas e arquivadas
- Total de membros por papel (professores, alunos, gestores)
- Total de tarefas criadas e avaliadas no período
- Taxa média de entrega de tarefas na organização
- Últimas atividades da organização

**Critérios de Aceite:**
- [ ] Dashboard exibe todas as métricas listadas
- [ ] Exportação em PDF disponível
- [ ] Dados filtrados por `organization_id` do JWT

---

### RF-18 — Dashboard do Gestor `[MVP]`

**Atores:** `GESTOR`  
**Módulo BE:** `reporting` | `GestorDashboardQuery`  
**Endpoint:** `GET /classrooms/{id}/dashboard`

**Métricas:**
- Lista de turmas gerenciadas com indicadores de saúde (% entregas, média de notas)
- Alunos com mais tarefas pendentes ou em atraso
- Comparativo de desempenho entre turmas

**Critérios de Aceite:**
- [ ] Gestor vê apenas turmas às quais está vinculado
- [ ] Indicadores de saúde calculados corretamente
- [ ] Alunos com pendências destacados

---

### RF-19 — Dashboard do Professor `[MVP]`

**Atores:** `PROFESSOR`  
**Módulo BE:** `reporting` | `TeacherDashboardQuery`  
**Endpoint:** `GET /subjects/{id}/dashboard`

**Métricas:**
- Submissões recebidas pendentes de avaliação (badge de contagem)
- Distribuição de notas da última tarefa avaliada
- Alunos que não entregaram a tarefa mais recente
- Média de notas por aluno na disciplina

**Critérios de Aceite:**
- [ ] Badge de pendências atualizado em tempo real
- [ ] Professor vê apenas dados das disciplinas que leciona
- [ ] Alunos sem entrega identificados claramente

---

### RF-20 — Dashboard do Aluno `[MVP]`

**Atores:** `ALUNO`  
**Módulo BE:** `reporting` | `StudentDashboardQuery`  
**Endpoint:** `GET /students/me/dashboard`

**Métricas:**
- Próximas tarefas com prazo (ordenadas por urgência)
- Tarefas entregues vs pendentes
- Últimas notas e feedbacks recebidos
- Média geral por disciplina

**Critérios de Aceite:**
- [ ] Aluno vê apenas seus próprios dados
- [ ] Tarefas ordenadas por urgência de prazo
- [ ] Médias calculadas corretamente por disciplina

---

# PARTE II — EVOLUÇÃO FUTURA

> Os requisitos desta seção **não serão implementados no MVP**. A arquitetura (Monolito Modular, Ports & Adapters, Domain Events) foi projetada para absorver estas funcionalidades como novos módulos sem reescrita do núcleo.

---

## Módulo: gamification — Gamificação

---

### RF-21 — Sistema de Pontos e Níveis `[FUTURO]`

**Descrição:** Alunos acumulam pontos ao concluir tarefas e acessar materiais. Pontos evoluem o nível do aluno dentro da organização.

**Módulo BE:** `gamification` | `AwardPointsUseCase` (consumidor de Domain Events)  
**Dependências:** `TaskSubmittedEvent`, `ContentAccessedEvent`

**Regras de Pontuação (preliminar):**
- Tarefa entregue no prazo: 100 pontos
- Tarefa entregue com atraso (se permitido): 50 pontos
- Nota máxima em uma tarefa: bônus de 50 pontos
- Acesso a material de conteúdo: 10 pontos (limite diário de 50)

> Pesos configuráveis por organização pelo ADMIN_ORG.

---

### RF-22 — Badges e Conquistas `[FUTURO]`

**Descrição:** Badges concedidas automaticamente ao aluno quando critérios predefinidos são atingidos.

**Módulo BE:** `gamification` | `CheckBadgeCriteriaUseCase`

**Exemplos:**
- "Entregou 10 tarefas seguidas no prazo"
- "Nota máxima em 5 tarefas"
- "Acessou conteúdo por 7 dias consecutivos"

---

### RF-23 — Ranking entre Alunos `[FUTURO]`

**Descrição:** Ranking de pontos por turma ou organização. Professores podem optar por tornar o ranking anônimo.

**Módulo BE:** `gamification` | `ClassroomRankingQuery`

---

## Módulo: ai — Inteligência Artificial

> Módulo `ai` implementado como Adapter (`AIPort`) isolado. Troca de provedor de LLM (OpenAI, Anthropic etc.) sem alteração nos Use Cases.

---

### RF-24 — Correção Automática por IA `[FUTURO]`

**Descrição:** Para tarefas dissertativas ou objetivas, o professor habilita correção automática. O sistema sugere nota e justificativa; o professor pode aceitar, editar ou rejeitar.

**Módulo BE:** `ai` | `AutoEvaluateSubmissionUseCase`  
**Dependências:** `assessment` (EvaluateSubmissionUseCase adaptado)

---

### RF-25 — Geração de Exercícios por IA `[FUTURO]`

**Descrição:** Professor informa o tópico e nível de dificuldade. Sistema gera exercícios via LLM. Professor revisa e publica como tarefa.

**Módulo BE:** `ai` | `GenerateExercisesUseCase`

---

### RF-26 — Assistente de Estudos para Aluno `[FUTURO]`

**Descrição:** Chat de IA contextualizado com o conteúdo da disciplina. Gera plano de estudos personalizado e sugere exercícios extras.

**Módulo BE:** `ai` | `StudyAssistantUseCase`, `GenerateStudyPlanUseCase`

---

# PARTE III — Requisitos Não Funcionais

| Categoria | Requisito | Decisão Técnica |
|---|---|---|
| **Segurança** | Senhas criptografadas | BCrypt fator 12 (`SEC-01`) |
| **Segurança** | RBAC | `@RolesAllowed` + `organization_id` do JWT (`SEC-05`, `SEC-07`) |
| **Segurança** | JWT | RS256, Access 15min, Refresh 7 dias Redis (`SEC-02`, `SEC-03`) |
| **Segurança** | Brute force | Rate limiting Redis: 5 tentativas/min por IP (`SEC-08`) |
| **Desempenho** | Tempo de resposta | < 2s (P95) para operações comuns |
| **Desempenho** | Virtual Threads | Java 21 Project Loom para I/O não bloqueante |
| **Usabilidade** | Responsivo | Tailwind CSS mobile-first |
| **Usabilidade** | Acessibilidade | Shadcn/ui (Radix UI) — WCAG 2.1 nível A |
| **Escalabilidade** | Modular | Monolito Modular com Bounded Contexts (`ADR-008`) |
| **Escalabilidade** | Storage | `StoragePort` abstrato (`ADR-004`) |
| **Escalabilidade** | Schema evolution | Flyway para migrations versionadas |
| **Testabilidade** | Cobertura mínima | 70% domínio + application (JaCoCo) |
| **Testabilidade** | Integração | Testcontainers MySQL + Redis |

---

## Glossário

| Termo | Definição |
|---|---|
| **Aggregate Root** | Entidade principal de um Bounded Context que controla as invariantes do domínio. |
| **Bounded Context** | Fronteira lógica onde um modelo de domínio específico se aplica. |
| **Domain Event** | Fato relevante ocorrido no domínio, publicado para comunicação assíncrona entre módulos. |
| **Port** | Interface que define um contrato entre o domínio e o mundo externo. |
| **Adapter** | Implementação concreta de um Port. Encapsula framework, banco ou serviço externo. |
| **Soft Delete** | Exclusão lógica: coluna `deleted_at` preenchida, registro permanece no banco. |
| **SDD** | Specification-Driven Development: especificação precede a implementação. |
| **RBAC** | Role-Based Access Control: controle de acesso baseado no papel do usuário. |
| **StoragePort** | Abstração de armazenamento de arquivos. Local no MVP, S3/MinIO no futuro. |

---

*Ver também: [Decisões Arquiteturais](../architecture/DECISIONS.md) | [ADRs](../architecture/adrs/)*
