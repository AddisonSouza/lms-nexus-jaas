---
name: fe-arch-review
description: Analisa o frontend (apps/web/src/) contra as specs do projeto e gera/substitui apps/web/ARCHITECTURE_BREAKS.md com todas as quebras de arquitetura encontradas. Use quando o usuário pedir para analisar, revisar ou auditar a arquitetura do frontend.
---

# Skill: Frontend Architecture Review

Analisa o código em `apps/web/src/` contra os contratos definidos em `docs/architecture/DECISIONS.md` e `docs/requirements/RF.md`, e gera o documento `apps/web/ARCHITECTURE_BREAKS.md`.

**Se o arquivo `apps/web/ARCHITECTURE_BREAKS.md` já existir, ele deve ser substituído completamente — não atualizado.**

---

## Passos

### 1. Ler as specs e decisões arquiteturais

Leia os seguintes arquivos antes de tocar no código:

- `docs/requirements/RF.md` — requisitos funcionais (fluxos, atores, contratos de dados, critérios de aceite)
- `docs/architecture/DECISIONS.md` — regras de código FE-01 a FE-12, regras SEC-*, DB-MT-*, stack obrigatória

Internalize especialmente a **seção 4 (Front-End)** do DECISIONS.md:
- Stack (FE-01 a FE-12)
- Estrutura de pastas esperada (4.2)
- Regras de segurança que impactam o FE (SEC-02, DB-MT-03)

### 2. Mapear todos os arquivos do frontend

Liste recursivamente `apps/web/src/` para ter o mapa completo de features, hooks, APIs, schemas, types e componentes.

### 3. Analisar quebras por categoria

Para cada categoria abaixo, leia os arquivos relevantes e identifique desvios:

#### Segurança
- `authStore.ts` e `axios.ts`: o Access Token está em memória (Zustand) ou em `localStorage`? (SEC-02 / RF-02 exige memória)
- `organizationId` vem do JWT ou é armazenado separadamente? (DB-MT-03)

#### Arquitetura de features (FE-11)
- Grep por `from '@features/` em arquivos fora da própria feature. Nenhum `features/X/` pode importar de `features/Y/`.
- Exportar a lista completa de violações com arquivo:linha.

#### Roteamento (FE-09)
- `routes.tsx`: todas as rotas existem? O login navega para uma rota definida?
- `ProtectedRoute`: verifica apenas autenticação ou também `role`?
- Rotas públicas (`/register`, `/forgot-password`, `/reset-password`) estão envolvidas em `PublicRoute`?

#### Estado e hooks (FE-03, FE-04, FE-06)
- `useEffect` é usado para fetch de dados? (FE-04: proibido — usar TanStack Query)
- Lógica de estado ou decodificação de JWT dentro de componentes em vez de hooks? (FE-03)
- Decoding manual de JWT (`atob`, `JSON.parse`) espalhado em componentes?

#### Tipos e contratos (FE-07, FE-08)
- Types em `types.ts` de cada feature batem com os contratos dos RFs? (ex: `SubmissionStatus` inclui `LATE`?)
- Respostas da API têm schemas Zod ou são tipadas apenas com interfaces TypeScript?

#### Layout e estrutura
- Existe `components/layout/` com AppShell/Sidebar/Header? (DECISIONS 4.2)
- As páginas usam um shell de navegação global ou cada uma é standalone?

#### Features ausentes
- Verificar se as features listadas no DECISIONS 4.2 (`dashboard`, `communication`) existem em `src/features/`
- Mapear quais RFs (RF-14 a RF-20) não têm implementação

#### Código (baixa prioridade)
- `queryFn` com chamada bruta de `api.get` inline em vez de função nomeada na camada `api/`
- Construção manual de URL base duplicando `axios.ts`
- `window.confirm()` para ações destrutivas (Shadcn `AlertDialog` é o esperado)
- Cores Tailwind brutas (`text-green-600`, `bg-gray-100`) em vez de tokens do design system (`text-success`, variáveis CSS do Shadcn)

### 4. Classificar por prioridade

Classifique cada quebra em:
- 🔴 **Crítica**: quebra de segurança ou que causa falha funcional imediata (ex: rota inexistente, token exposto)
- 🟠 **Alta**: viola contrato arquitetural com impacto direto na manutenibilidade ou RBAC
- 🟡 **Média**: viola spec mas não causa falha imediata (feature ausente, layout faltando)
- 🟢 **Baixa**: desvio de convenção sem impacto funcional

### 5. Gerar o documento

**Substitua completamente** o arquivo `apps/web/ARCHITECTURE_BREAKS.md` com o seguinte formato:

```markdown
# Quebras de Arquitetura — Frontend (apps/web)

> Documento gerado em YYYY-MM-DD. Código analisado: `apps/web/src/` (branch `<branch-atual>`).
> Specs de referência: `docs/architecture/DECISIONS.md` · `docs/requirements/RF.md`.

---

## Índice por Prioridade

| # | Prioridade | Categoria | Resumo |
|---|---|---|---|
| 1 | 🔴 Crítica | ... | ... |
...

---

## 1. 🔴 [Título da quebra]

**Regra violada:** [ID da regra] — *"citação exata da regra"*

**Arquivos:**
- `caminho/do/arquivo.ts` (linha N)

**Problema:**
[Descrição objetiva do desvio, com trecho de código ERRADO se relevante]

**Correção esperada:**
[O que deve ser feito — comportamento correto esperado]

---

## 2. ...
```

---

## Critérios de qualidade do documento

- Cada item deve referenciar a regra violada pelo ID (FE-11, SEC-02, etc.) com citação do texto original
- Incluir arquivo e linha sempre que possível
- Mostrar trecho de código ERRADO quando ajuda a entender o problema
- A correção esperada deve ser específica o suficiente para outro agente implementar sem perguntas
- Ordenar por prioridade: críticas primeiro, baixas por último
- Não inventar quebras — só reportar o que foi verificado no código
- Atualizar o índice para refletir exatamente os itens gerados

---

## Nota sobre substituição

O arquivo `apps/web/ARCHITECTURE_BREAKS.md` **sempre deve ser substituído** ao rodar esta skill, não appendado. Use a ferramenta `Write` (não `Edit`) para garantir a substituição completa. Se o arquivo não existir, ele será criado.
