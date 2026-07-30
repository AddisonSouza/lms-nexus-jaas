# Tela inicial sem forçar criação de organização — Delta

## Added

- **Tela inicial de boas-vindas** (`/welcome`, fora do `AppShell`): usuário
  autenticado sem organização vê os dois caminhos com o mesmo destaque — criar
  uma organização ou entrar em uma existente — e um aviso de que o administrador
  ou gestor de uma organização existente pode convidá-lo por e-mail.
- **Entrada por link de convite**: campo que aceita o link recebido por e-mail ou
  o token puro, valida contra o formato UUID emitido pelo back-end e encaminha
  para `/invitations/:token/accept`, que segue responsável pela validação real
  (organização, papel, expiração). Link inválido exibe erro e não navega.
- **Estado vazio nas áreas internas**: sem organização, as rotas que dependem
  dela renderizam um aviso com os dois caminhos, em vez de uma tela quebrada.
  Antes, só a rota `/` tratava o caso — abrir `/classrooms` direto pela URL
  levava a uma página sem dados.

## Changed

- **Destino do usuário sem organização**: `/` redirecionava para
  `/organizations/new`, tornando a criação de organização obrigatória na
  prática. Agora redireciona para `/welcome`. Com organização, nada muda
  (`/` → `/classrooms`).

## Notes

- Mudança só de front-end: nenhum endpoint novo. O back-end não expõe listagem
  de convites pendentes nem entrada em organização por código, o que definiu o
  formato do caminho de convite.
- Nova feature `apps/web/src/features/onboarding/`. O guard vive em `app/`
  (`RequireOrganization`) para que nenhuma feature precise importar `onboarding`.
- Fora de escopo, registrado em `questions.md`: a `/welcome` não tem Header,
  logo não oferece "sair" — candidato a card próprio.
