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

### Requirement: Telas públicas de autenticação têm saída para o login
As rotas públicas que não são o próprio login (`/register`, `/forgot-password`, `/reset-password`, `/confirm-email`) SHALL exibir um controle de retorno consistente (`← Voltar ao login`) que leva a `/login`, inclusive nos estados sem formulário. O `/login` não exibe o controle — é o destino. As telas do fluxo de organização (`/welcome`, `/organizations/new`) e o aceite de convite seguem com a saída própria do `MinimalHeader`.

#### Scenario: Tela pública com formulário
- **WHEN** usuário abre `/register`, `/forgot-password` ou `/reset-password` com token válido
- **THEN** sistema exibe `Voltar ao login` acima do título, apontando para `/login`
- **AND** no `/register` o rodapé "Já tem conta? Entrar" permanece, por ser convite contextual e não navegação de retorno

#### Scenario: Estado terminal sem formulário
- **WHEN** a tela chega a um estado sem formulário — "E-mail enviado" do `/forgot-password`, "Link de redefinição inválido." do `/reset-password`, "Confirme seu e-mail", "Link inválido ou expirado" ou "E-mail já confirmado" do `/confirm-email`
- **THEN** o controle de retorno continua visível, porque nesses estados ele é a única saída da tela

#### Scenario: Estados que já redirecionam sozinhos
- **WHEN** o `/confirm-email` está confirmando o token ou acabou de confirmar com sucesso
- **THEN** o controle não é exibido, porque a própria tela leva ao `/login` em seguida

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
O sistema SHALL exibir no `Header` — e no `MinimalHeader`, quando há sessão — o nome do usuário autenticado (`userName` do `authStore`), ou o e-mail (`userEmail`) quando o token não traz nome, e um botão de logout. O identificador interno (UUID) MUST NOT ser exibido. Nomes longos são truncados, com o valor inteiro no `title`.

#### Scenario: Usuário com nome
- **WHEN** usuário autenticado cujo token traz `name: 'Ana Souza'` visualiza o Header
- **THEN** sistema exibe "Ana Souza" no canto superior direito, sem nenhum trecho do UUID

#### Scenario: Token sem nome
- **WHEN** o token traz `email` mas não `name`
- **THEN** sistema exibe o e-mail no lugar do nome

#### Scenario: Nome longo
- **WHEN** o nome não cabe na largura reservada
- **THEN** o texto é truncado com reticências e o nome inteiro aparece no tooltip (`title`)

#### Scenario: Clique em logout
- **WHEN** usuário clica em "Sair" no Header
- **THEN** sistema chama `POST /auth/logout`, limpa o `authStore` e o cache do React Query e redireciona para `/login`

#### Scenario: Outra conta entra depois do Sair
- **WHEN** usuário sai e outra conta entra sem recarregar a página
- **THEN** o seletor de organização e as demais telas mostram só os dados da nova conta, nunca os da anterior
