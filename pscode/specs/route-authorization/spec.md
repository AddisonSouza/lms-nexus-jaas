# route-authorization Specification

## Purpose
Autorização de rotas no frontend: ProtectedRoute valida autenticação e papel do usuário antes de renderizar a rota.

## Requirements
### Requirement: ProtectedRoute verifica papel além de autenticação
O sistema SHALL bloquear acesso a rotas restritas quando o papel do usuário autenticado não estiver na lista de papéis permitidos para aquela rota.

#### Scenario: Usuário sem papel autorizado tenta acessar rota restrita
- **WHEN** usuário com `role === 'ALUNO'` tenta acessar `/assessment/tasks` (restrita a `['PROFESSOR']`)
- **THEN** sistema redireciona para `/` sem exibir a página

#### Scenario: Usuário com papel autorizado acessa rota restrita
- **WHEN** usuário com `role === 'PROFESSOR'` acessa `/assessment/tasks`
- **THEN** sistema exibe a página normalmente

#### Scenario: Rota sem restrição de papel
- **WHEN** qualquer usuário autenticado acessa rota sem `roles` especificados (ex: `/classrooms`)
- **THEN** sistema exibe a página — apenas autenticação é exigida

---

### Requirement: Mapeamento de papéis por rota
O sistema SHALL aplicar as seguintes restrições de papel por rota:

| Rota | Papéis permitidos |
|---|---|
| `/assessment/tasks` | `PROFESSOR` |
| `/assessment/student-tasks` | `ALUNO` |
| `/curriculum` | `ADMIN_ORG`, `GESTOR`, `PROFESSOR`, `ALUNO` |
| `/organizations/new` | qualquer autenticado |
| `/classrooms` | qualquer autenticado |

#### Scenario: ALUNO acessa rota de student-tasks
- **WHEN** usuário com `role === 'ALUNO'` acessa `/assessment/student-tasks`
- **THEN** sistema exibe a página

#### Scenario: PROFESSOR acessa rota de student-tasks
- **WHEN** usuário com `role === 'PROFESSOR'` tenta acessar `/assessment/student-tasks`
- **THEN** sistema redireciona para `/`

---

### Requirement: Rotas internas exigem organização
O sistema SHALL exibir um estado vazio, em vez da página, quando o usuário
autenticado acessar uma rota que depende de organização sem possuir
`organizationId`. O estado vazio SHALL oferecer os dois caminhos: criar uma
organização ou entrar em uma existente por convite.

#### Scenario: Usuário sem organização acessa rota interna pela URL
- **WHEN** usuário autenticado sem `organizationId` acessa `/classrooms`
- **THEN** sistema exibe o aviso de que ele não faz parte de nenhuma organização,
  sem renderizar a listagem

#### Scenario: Usuário com organização acessa rota interna
- **WHEN** usuário autenticado com `organizationId` acessa `/classrooms`
- **THEN** sistema exibe a página normalmente

#### Scenario: Rota raiz fica fora do guard
- **WHEN** usuário autenticado sem `organizationId` acessa `/`
- **THEN** sistema redireciona para `/welcome` em vez de exibir o estado vazio

---

### Requirement: PublicRoute redireciona usuário autenticado
O sistema SHALL redirecionar automaticamente o usuário autenticado que tenta acessar rotas públicas (login, registro, recuperação de senha) de volta para o app.

#### Scenario: Usuário autenticado acessa /login
- **WHEN** usuário com sessão ativa tenta acessar `/login`
- **THEN** sistema redireciona para `/`

#### Scenario: Usuário autenticado acessa /register
- **WHEN** usuário com sessão ativa tenta acessar `/register`
- **THEN** sistema redireciona para `/`

#### Scenario: Usuário não autenticado acessa /register
- **WHEN** usuário sem sessão acessa `/register`
- **THEN** sistema exibe o formulário de registro normalmente
