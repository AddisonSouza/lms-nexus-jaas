# onboarding-welcome Specification

## Purpose
Tela inicial para o usuário autenticado que ainda não pertence a nenhuma
organização: apresenta os caminhos possíveis em vez de forçar a criação de uma
organização.

## Requirements
### Requirement: Usuário sem organização cai na tela de boas-vindas
O sistema SHALL direcionar o usuário autenticado sem `organizationId` para
`/welcome`, e NÃO SHALL forçá-lo ao formulário de criação de organização.

#### Scenario: Login sem organização vinculada
- **WHEN** usuário autenticado sem claim `org` no JWT acessa `/`
- **THEN** sistema redireciona para `/welcome`

#### Scenario: Login com organização vinculada
- **WHEN** usuário autenticado com `organizationId` acessa `/`
- **THEN** sistema redireciona para `/classrooms` — comportamento inalterado

---

### Requirement: Dois caminhos com o mesmo destaque
A tela SHALL oferecer criar uma organização e entrar em uma existente com peso
visual equivalente, sem apresentar nenhum dos dois como obrigatório, e SHALL
avisar que o administrador ou gestor de uma organização existente pode convidar
o usuário por e-mail.

#### Scenario: Usuário escolhe criar organização
- **WHEN** usuário aciona "Criar organização" em `/welcome`
- **THEN** sistema navega para `/organizations/new` com o formulário existente

#### Scenario: Usuário ainda não recebeu convite
- **WHEN** usuário abre `/welcome`
- **THEN** a tela exibe o aviso de que o convite chega por e-mail enviado pelo
  administrador ou gestor da organização

---

### Requirement: Entrada por link de convite
A tela SHALL aceitar o link de convite recebido por e-mail ou o token puro,
extrair o token, validá-lo contra o formato UUID emitido pelo back-end e
encaminhar para a tela de aceite. A validação de organização, papel e expiração
permanece com `/invitations/:token/accept`.

#### Scenario: Link completo colado
- **WHEN** usuário cola `https://<host>/invitations/<uuid>/accept` e submete
- **THEN** sistema navega para `/invitations/<uuid>/accept`

#### Scenario: Token puro informado
- **WHEN** usuário informa apenas o `<uuid>` do convite e submete
- **THEN** sistema navega para `/invitations/<uuid>/accept`

#### Scenario: Valor inválido
- **WHEN** usuário submete um texto que não contém um token em formato UUID
- **THEN** sistema exibe erro de validação e permanece em `/welcome`
