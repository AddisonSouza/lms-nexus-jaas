# app-layout Specification

## Purpose
Layout persistente (AppShell com Header e Sidebar) aplicado às rotas autenticadas, e sua ausência nas rotas públicas.

## Requirements
### Requirement: AppShell envolve todas as rotas autenticadas
O sistema SHALL renderizar um layout persistente (`AppShell`) composto por `Header` e `Sidebar` em todas as rotas que exigem autenticação. Rotas públicas (`/login`, `/register`, `/forgot-password`, `/reset-password`, `/confirm-email`) não devem renderizar o AppShell.

#### Scenario: Usuário autenticado acessa rota protegida
- **WHEN** usuário autenticado navega para qualquer rota protegida (ex: `/classrooms`, `/curriculum`)
- **THEN** sistema exibe `Header` no topo e `Sidebar` lateral sem que a página precise declarar esses elementos

#### Scenario: Usuário não autenticado acessa rota pública
- **WHEN** usuário não autenticado acessa `/login`
- **THEN** sistema exibe apenas o formulário de login sem `Sidebar` nem `Header`

---

### Requirement: Sidebar exibe navegação contextual ao papel
O sistema SHALL exibir links de navegação na `Sidebar` de acordo com o papel (`role`) do usuário autenticado, lido do `authStore`.

#### Scenario: Usuário com papel PROFESSOR
- **WHEN** usuário com `role === 'PROFESSOR'` visualiza a Sidebar
- **THEN** sistema exibe links: Turmas, Disciplinas, Tarefas (para professor)

#### Scenario: Usuário com papel ALUNO
- **WHEN** usuário com `role === 'ALUNO'` visualiza a Sidebar
- **THEN** sistema exibe links: Turmas, Minhas Tarefas

#### Scenario: Usuário com papel ADMIN_ORG ou GESTOR
- **WHEN** usuário com `role === 'ADMIN_ORG'` ou `role === 'GESTOR'` visualiza a Sidebar
- **THEN** sistema exibe links: Turmas, Disciplinas, Tarefas, Membros

---

### Requirement: Header exibe nome do usuário e ação de logout
O sistema SHALL exibir no `Header` o nome ou email do usuário autenticado e um botão de logout.

#### Scenario: Clique em logout
- **WHEN** usuário clica em "Sair" no Header
- **THEN** sistema chama `POST /auth/logout`, limpa o `authStore` e redireciona para `/login`
