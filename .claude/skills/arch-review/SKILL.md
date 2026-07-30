---
name: arch-review
description: Analisa o código do projeto contra as specs e decisões arquiteturais e gera/substitui ARCHITECTURE_BREAKS.md no diretório alvo. Aceita argumento: "fe" (frontend), "be" (backend) ou sem argumento para ambos. Use quando o usuário pedir para analisar, revisar, auditar ou checar quebras de arquitetura.
---

# Skill: Architecture Review

Analisa o código-fonte contra os contratos definidos em `docs/architecture/DECISIONS.md` e `docs/requirements/RF.md`, e gera o documento `ARCHITECTURE_BREAKS.md` no diretório do alvo.

**Se o arquivo `ARCHITECTURE_BREAKS.md` já existir no diretório alvo, ele DEVE ser substituído completamente — nunca appendado ou atualizado parcialmente. Use sempre a ferramenta `Write` (não `Edit`).**

---

## Input

O usuário pode invocar com argumento opcional:
- `/arch-review fe` — analisa somente o frontend (`apps/web/`)
- `/arch-review be` — analisa somente o backend (`apps/api/`)
- `/arch-review` (sem argumento) — analisa ambos, em sequência: frontend primeiro, depois backend

Se o argumento for ambíguo (ex: "front", "web", "frontend") trate como `fe`. Se for "back", "api", "backend" trate como `be`.

---

## Passo 0 — Ler as specs antes de qualquer coisa

Leia os dois arquivos abaixo **antes de tocar no código-fonte**. Eles definem todas as regras que serão verificadas:

- `docs/requirements/RF.md` — requisitos funcionais (fluxos, atores, contratos de dados, critérios de aceite)
- `docs/architecture/DECISIONS.md` — regras FE-01..FE-12, RD-01..RD-08, MOD-01..MOD-05, SEC-01..SEC-08, LOM-01..LOM-07, TEST-*, ADR-*, stack obrigatória, estrutura de pastas esperada

---

## Alvo: Frontend (`fe`)

### Diretório analisado
`apps/web/src/`

### Arquivo de saída
`apps/web/ARCHITECTURE_BREAKS.md` — substituir completamente se existir.

### Categorias a verificar

#### Segurança (SEC-02, DB-MT-03)
- `authStore.ts` e `axios.ts`: o `accessToken` está em memória (Zustand sem persist) ou em `localStorage`/`sessionStorage`? SEC-02 exige memória apenas.
- `organizationId` vem do JWT ou é armazenado separadamente? DB-MT-03 proíbe armazenar `org_id` no cliente além do JWT.
- Refresh token interceptor: após refresh, o novo access token é atualizado no Zustand ou descartado?

#### Isolamento de features (FE-11)
- Grep por `from '@features/` em arquivos dentro de `features/`. Nenhum `features/X/` pode importar de `features/Y/`.
- Listar cada violação com arquivo:linha.

#### Roteamento (FE-09)
- `routes.tsx`: todas as rotas referenciam componentes existentes? O login navega para uma rota definida?
- Rotas protegidas estão envoltas em `ProtectedRoute` (verifica auth + role)?
- Rotas públicas (`/register`, `/forgot-password`, `/reset-password`) estão envoltas em `PublicRoute`?
- Existe rota `/` (root)?

#### Estado e hooks (FE-03, FE-04, FE-06)
- `useEffect` é usado para fetch de dados? (FE-04: proibido — usar TanStack Query `useQuery`/`useMutation`)
- Decodificação de JWT (`atob`, `JSON.parse(base64)`) inline em componentes em vez de hook dedicado?
- Lógica de negócio (chamadas de API, `useMutation`) dentro de componentes de apresentação em vez de hooks? (FE-03)

#### Componentes de UI (ADR-006)
- Modais com overlay `fixed inset-0` customizado em vez de `<Dialog>` do Shadcn/Radix?
- Drawers/sidebars customizados em vez de `<Sheet>` do Shadcn/Radix?
- `window.confirm()` para confirmação de ações destrutivas em vez de `<AlertDialog>` do Shadcn?
- Cores Tailwind brutas (`text-green-600`, `bg-gray-100`, `text-red-500`) em vez de tokens do design system (`text-destructive`, `bg-muted`, variáveis CSS do Shadcn)?

#### Tipos e contratos (FE-07, FE-08)
- Types em `types.ts` de cada feature batem com os contratos dos RFs? (ex: `SubmissionStatus` inclui `LATE`?)
- Respostas da API têm schemas Zod ou são tipadas apenas com interfaces TypeScript?
- Funções de `api/` usadas diretamente no `queryFn` ou inline com `api.get()`/`api.post()`?

#### Layout e estrutura (DECISIONS 4.2)
- Existe `components/layout/` com AppShell/Sidebar/Header?
- Existe `components/ui/` para componentes do design system?
- As páginas usam shell de navegação global ou cada uma é standalone?

#### Features ausentes vs. RFs
- Verificar se as features listadas no DECISIONS 4.2 existem em `src/features/`
- Mapear quais RFs não têm implementação no frontend

#### Qualidade de código (baixa prioridade)
- `queryFn` com `api.get()` inline em vez de função nomeada na camada `api/`
- `API_BASE_URL` ou similar construído manualmente duplicando `axios.ts`
- Comentários `// eslint-disable` sem justificativa
- Providers ausentes em `providers.tsx` (Auth, Theme, OrgContext)

---

## Alvo: Backend (`be`)

### Diretório analisado
`apps/api/src/main/java/`

### Arquivo de saída
`apps/api/ARCHITECTURE_BREAKS.md` — substituir completamente se existir.

### Categorias a verificar

#### Segurança (ENV-04, SEC-*)
- Existe `privateKey.pem` ou `publicKey.pem` em `src/main/resources/`? ENV-04 proíbe chaves no repositório.
- `quarkus.swagger-ui.always-include=true` em `application.properties`? Deve ser restrito ao perfil `%dev`.
- `org_id` extraído do JWT ou aceito via request body? SEC-05 proíbe body.

#### Camadas (RD-01..RD-08)
- Grep por imports de `infrastructure/` dentro de `application/usecase/`. RD-03/RD-04: `application/` importa apenas `domain/`.
- Grep por imports de `domain/port/out/` (repositórios) dentro de `interfaces/rest/`. RD-06: controllers chamam apenas use cases.
- Grep por imports de `infrastructure/` ou `interfaces/` dentro de `domain/`. RD-01/RD-02: domain não importa nada externo.
- Para cada injeção em controllers: é um Use Case (Port in) ou um repositório/service concreto?

#### Módulos (MOD-01..MOD-05)
- Grep por imports de `module/X/` dentro de `module/Y/` (X ≠ Y). Cada cross-module import é uma violação.
- Listar com arquivo:linha e os dois módulos envolvidos.

#### MapStruct (ADR-005)
- Grep por métodos privados `toEntity`, `toDomain`, `toResponse`, `toDto` em `RepositoryImpl` e `*Service`. Todos devem usar mappers `@Mapper(componentModel = "cdi")`.
- Verificar se todos os `*Mapper.java` com `@Mapper` são realmente injetados e usados nos repositórios e serviços correspondentes.

#### Lombok (LOM-01..LOM-07)
- `@Data` em classes de `domain/model/`? Permitido apenas em `infrastructure/persistence/`.
- `@Value` em entidades mutáveis? Deve ser apenas em Value Objects.
- `@Builder` sem `@AllArgsConstructor(access = AccessLevel.PRIVATE)` em Value Objects?

#### DDD — comportamento das entidades
- Entidades de `domain/model/` têm métodos de comportamento (`publish()`, `evaluate()`, `close()`) ou são apenas containers de dados com `toBuilder()` externo?
- Invariantes de estado (ex: `if status != DRAFT throw`) estão no serviço ou na entidade?

#### Domain Exceptions
- `IllegalArgumentException` ou `IllegalStateException` usados para regras de negócio em `application/usecase/`? Devem ser exceptions em `domain/exception/`.

#### Domain Events (MOD-02)
- Eventos disparados (`Event.fire()`) batem com a ação realizada? (ex: `TaskPublishedEvent` em publish, não `TaskCreatedEvent`)

#### Testes (TEST-BE-01..TEST-BE-04)
- Existe `@QuarkusTest` + Testcontainers? Ou `application.properties` de teste aponta para banco hardcoded?
- Testes de domínio (`*Test.java` sem `@QuarkusTest`) para modelos em `domain/model/`?
- Testes de integração cobrem fluxos reais ou apenas verificam 401?

#### Qualidade / SRP
- `GlobalExceptionMapper` importa de quantos módulos? Tem > 20 `instanceof` checks? SRP violation.
- Lógica de autorização inline em controllers (ex: `if (!task.getCreatedBy().equals(userId)) throw`) em vez de Use Case?

---

## Classificação de prioridade

Classifique cada quebra encontrada em:
- 🔴 **Crítica**: quebra de segurança, chave comprometida, ou falha funcional imediata
- 🟠 **Alta**: viola contrato arquitetural com impacto direto na manutenibilidade, testabilidade ou RBAC
- 🟡 **Média**: viola spec mas não causa falha imediata (feature ausente, padrão errado mas funciona)
- 🟢 **Baixa**: desvio de convenção sem impacto funcional

---

## Formato do documento de saída

```markdown
# Quebras de Arquitetura — [Frontend|Backend] ([apps/web|apps/api])

> Documento gerado em YYYY-MM-DD. Código analisado: `[apps/web/src/|apps/api/src/main/java/]` (branch `<branch-atual>`).
> Specs de referência: `docs/architecture/DECISIONS.md` · `docs/requirements/RF.md`.

---

## Índice por Prioridade

| # | Prioridade | Categoria | Resumo |
|---|---|---|---|
| 1 | 🔴 Crítica | Segurança | ... |
...

---

## 1. 🔴 [Título da quebra]

**Regra violada:**
- [ID] — *"citação exata da regra do DECISIONS.md"*

**Arquivos:**
- `caminho/do/arquivo.ts` (linha N)

**Problema:**
[Descrição objetiva. Incluir trecho de código ERRADO se ajuda a entender.]

```language
// trecho errado, se relevante
```

**Correção esperada:**
[O que deve ser feito — específico o suficiente para outro agente implementar sem perguntar.]

---

## 2. ...

---

## Resumo de Regras Violadas

| Regra | Descrição | Itens |
|---|---|---|
| FE-11 | ... | #3, #7 |
...

---

*Análise realizada em YYYY-MM-DD. Branch: `<branch>`.*
```

---

## Critérios de qualidade

- Referenciar a regra pelo ID exato (FE-11, RD-03, MOD-01, etc.) com citação do texto do DECISIONS.md
- Incluir arquivo e número de linha sempre que possível
- Mostrar trecho de código ERRADO quando ajuda a entender o problema
- Correção esperada deve ser específica o suficiente para outro agente implementar sem perguntas adicionais
- Ordenar dentro de cada alvo: críticas primeiro, baixas por último
- **Não inventar quebras** — só reportar o que foi verificado no código
- Atualizar o índice para refletir exatamente os itens gerados
- Se uma categoria não tiver violações, omiti-la do documento (não listar "nenhuma violação encontrada")

---

## Nota sobre substituição

O arquivo `ARCHITECTURE_BREAKS.md` **sempre deve ser substituído completamente** ao rodar esta skill, nunca appendado ou editado parcialmente.

- Use a ferramenta `Write` (não `Edit`) para garantir a substituição completa.
- Se o arquivo não existir, ele será criado.
- Se existir com conteúdo anterior (de uma análise anterior), esse conteúdo **deve ser descartado** — o novo documento reflete o estado atual do código.
