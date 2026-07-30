# organic-design-system Specification

## Purpose
Design system "Organic": tokens de cor, tipografia, espaçamento, raio e sombra expostos como custom properties CSS e consumidos pelo Tailwind.

## Requirements
### Requirement: Tokens de design Organic
O sistema SHALL expor a paleta, tipografia, espaçamento, raio de borda e sombra do design system "Organic" como custom properties CSS (`--color-*`, `--font-heading`, `--font-body`, `--space-*`, `--radius-*`, `--shadow-*`) em `apps/web/src/index.css`, consumidas pelo Tailwind (`tailwind.config.js`) sem envolver o valor da variável em uma função de cor incompatível (ex.: `hsl()` sobre um valor já expresso em outro espaço de cor).

#### Scenario: Tokens aplicados no tema claro
- **WHEN** a aplicação carrega sem a classe `dark` na raiz do documento
- **THEN** os elementos renderizados usam a paleta clara do Organic (fundo cream, texto escuro, acento terracota) definida em `:root`

#### Scenario: Tokens aplicados no tema escuro
- **WHEN** a classe `dark` está presente na raiz do documento
- **THEN** os elementos renderizados usam a paleta escura do Organic definida no seletor `.dark`, sem misturar valores do tema claro

### Requirement: Alternância de tema persistida
O sistema SHALL permitir ao usuário alternar entre tema claro e escuro através de um controle no `Header`, aplicando a classe `dark` na raiz do documento e persistindo a preferência escolhida em `localStorage`, restaurando-a em carregamentos futuros.

#### Scenario: Usuário alterna para tema escuro
- **WHEN** o usuário aciona o controle de tema estando no tema claro
- **THEN** a interface passa a usar os tokens do tema escuro e a preferência "escuro" é persistida em `localStorage`

#### Scenario: Preferência de tema restaurada em novo carregamento
- **WHEN** o usuário recarrega a aplicação após ter escolhido o tema escuro anteriormente
- **THEN** a aplicação inicia já no tema escuro, sem exigir nova alternância manual

#### Scenario: Nenhuma preferência salva ainda
- **WHEN** o usuário acessa a aplicação pela primeira vez, sem preferência de tema em `localStorage`
- **THEN** a aplicação inicia no tema claro por padrão

### Requirement: Primitivos de UI compartilhados
O sistema SHALL disponibilizar em `apps/web/src/components/ui/` os primitivos `Button`, `Card`, `Badge`, `Input`, `Textarea`, `Table` e `Segmented`, todos consumindo exclusivamente os tokens de design Organic (nunca cores ou raios fixos embutidos no componente), para que as páginas de feature os componham em vez de recriar marcação equivalente com classes Tailwind soltas.

#### Scenario: Botão usa variantes do design system
- **WHEN** uma página renderiza `Button` com uma das variantes suportadas (primário, secundário, ghost, ícone)
- **THEN** o botão é exibido com formato pill e cores derivadas dos tokens `--color-accent`/`--color-surface`, sem valores de cor hardcoded no local de uso

#### Scenario: Cartão de estatística reutiliza o primitivo Card
- **WHEN** um dashboard renderiza um cartão de métrica (ex.: contagem de tarefas, média de notas)
- **THEN** o cartão usa o primitivo `Card` (com variante de elevação), não uma `div` com `border`/`rounded-lg` escritos manualmente

### Requirement: Reskin visual sem regressão funcional
O sistema SHALL preservar todo o comportamento, chamadas de API, validações e regras de negócio das páginas existentes durante o reskin — apenas marcação, classes CSS e tokens visuais mudam.

#### Scenario: Suíte de testes de frontend após o reskin
- **WHEN** a suíte Vitest do frontend é executada após a aplicação dos novos primitivos e tokens
- **THEN** todos os testes que validam comportamento (submissão de formulário, navegação, chamadas de API mockadas) continuam passando, com ajustes permitidos apenas em asserções sobre classes/estrutura de DOM que dependiam do markup antigo
