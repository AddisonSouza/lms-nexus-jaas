# Tela inicial sem forçar criação de organização

## Summary

Hoje quem se cadastra e ainda não pertence a nenhuma organização é jogado direto
no formulário de criação, como se criar uma organização fosse obrigatório. Passa
a existir uma tela inicial de boas-vindas que oferece os dois caminhos com o
mesmo destaque — criar uma organização ou entrar em uma existente pelo convite —
e avisa que o administrador ou gestor de uma organização já existente pode
convidá-lo por e-mail. Quem tenta abrir uma área interna sem organização vê um
aviso explicando a situação, em vez de uma tela quebrada.

## Technical detail

- Nova feature `apps/web/src/features/onboarding/`, que junta os dois caminhos
  sem criar import cruzado entre `organization` e `invitation`.
- `WelcomePage` em rota própria `/welcome`, fora do `AppShell` — mesmo padrão de
  `/organizations/new`, sem Sidebar apontando para áreas indisponíveis.
- Dois caminhos em destaque equivalente, um ao lado do outro: **criar
  organização** (CTA direto para `/organizations/new`, uma ação) e **entrar via
  convite** (campo para colar o link). Nenhum dos dois é apresentado como
  obrigatório ou como o caminho padrão.
- Aviso no caminho do convite: o administrador ou gestor de uma organização já
  existente pode convidar o usuário por e-mail, e o link chega na mensagem. O
  texto cobre quem ainda não recebeu convite nenhum e não tem o que colar.
- `RootRedirect.tsx` passa a mandar o usuário sem `organizationId` para
  `/welcome` (hoje: `/organizations/new`). Com organização, nada muda.
- Caminho do convite: campo que aceita o link (ou o token) do convite, valida
  com Zod, extrai o token e navega para `/invitations/:token/accept`, que já
  existe e faz a validação real. Nenhum endpoint novo — o back-end não expõe
  listagem de convites pendentes nem entrada por código.
- Áreas internas: guard `RequireOrganization` em `app/`, aplicado às rotas do
  `AppShell` que dependem de organização. Sem `organizationId`, renderiza o
  estado vazio `NoOrganizationState` com os dois caminhos. Fica em `app/` para
  que nenhuma feature importe `onboarding`.
- Formulário com React Hook Form + Zod, ícones só de `lucide-react`, componentes
  do design system existente (`Card`, `Button`, `CardKicker`). TS strict.

## Scope

### In

- Feature `onboarding` com `WelcomePage` e `NoOrganizationState`.
- Rota `/welcome` e mudança do `RootRedirect`.
- Campo de link de convite com parsing do token e navegação.
- Aviso sobre convite por e-mail vindo do admin/gestor da organização.
- Guard `RequireOrganization` nas rotas internas do `AppShell`.
- Testes de componente (Vitest + Testing Library) para os itens acima.

### Out

- Formulário de criação de organização (`CreateOrganizationForm`) — inalterado.
- Fluxo de convite em si: emissão, e-mail, validação e aceite no back-end.
- Qualquer mudança no back-end, incluindo listar convites pendentes ou entrar
  em organização por código.
- Botão de "sair" na `/welcome` (a tela não tem Header) — card separado se virar
  problema.

## Subtasks

- [x] Criar `features/onboarding/components/WelcomePage.tsx` com os dois cards em
      destaque equivalente (criar organização → `/organizations/new`; entrar via
      convite) e o aviso de que o admin/gestor de uma organização existente pode
      convidar por e-mail; registrar a rota `/welcome` fora do `AppShell` e
      apontar o `RootRedirect` para ela.
- [x] Adicionar ao `WelcomePage` o campo de link de convite: schema Zod que
      aceita link completo ou token, extração do token e navegação para
      `/invitations/:token/accept`, com erro de validação visível.
- [x] Criar `NoOrganizationState` em `onboarding` e o guard
      `app/RequireOrganization.tsx`, aplicando-o às rotas do `AppShell` que
      dependem de organização.
- [x] Cobrir com testes: redirect do `RootRedirect`, parsing do link de convite
      (válido e inválido) e o guard renderizando o estado vazio.
