---
name: pr-review-merge
description: Fluxo completo de PR — encontra o PR aberto (da branch atual ou por número), revisa o diff, corrige problemas encontrados, commita, faz push, resolve conflitos com a base e mergeia o PR. Use quando o usuário pedir para "revisar PR", "revisar e mergear PR", "fechar PR" ou equivalente — é mais abrangente que apenas revisar (/review, /code-review): este fluxo também corrige, commita, push e mergeia.
---

# Skill: Revisar, Corrigir e Mergear PR

Fluxo ponta a ponta para um PR aberto: localizar → revisar → corrigir problemas → commitar → push → resolver conflitos com a base → mergear.

**Esta skill executa ações que afetam estado remoto compartilhado (push e merge). Ela só deve parar para confirmar com o usuário nos casos de bloqueio listados no Passo 6 — fora disso, o fluxo é automático, pois esse é o propósito explícito desta skill.**

---

## Ambiente deste projeto (não usar `gh` puro)

- O CLI `gh` **não existe no WSL**. Use sempre o binário do Windows:
  ```bash
  GHEXE="/mnt/c/Program Files/GitHub CLI/gh.exe"
  ```
- Sempre passe `--repo OWNER/REPO` explicitamente em todo comando `$GHEXE pr ...`. Sem isso, o `gh.exe` tenta detectar o repositório via git local e pode falhar com `unable to find git executable in PATH`.
- Descubra `OWNER/REPO` a partir do remoto:
  ```bash
  git remote get-url origin   # git@github.com:OWNER/REPO.git → extrair OWNER/REPO
  ```
- Se algum comando `$GHEXE` falhar com `unable to find git executable in PATH` (acontece tipicamente em `pr create`, às vezes em `pr merge` se ele tentar sincronizar a branch local depois), caia para `curl` na REST API usando o token do próprio `gh.exe`:
  ```bash
  TOKEN=$("$GHEXE" auth token)
  curl -s -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
    https://api.github.com/repos/OWNER/REPO/...
  ```
  (Ver memória `feedback-github-pr-curl` para o padrão de criação de PR via curl, se for necessário criar um PR como parte do fluxo.)

---

## Passo 1 — Localizar o PR alvo

- Se o usuário passou um número (`/pr-review-merge 41` ou "revisa o PR 41"), use esse número direto.
- Caso contrário, detecte pela branch atual:
  ```bash
  $GHEXE pr list --repo OWNER/REPO --head "$(git branch --show-current)" --state open --json number,title,headRefName
  ```
- Se não houver PR para a branch atual, liste os PRs abertos (`$GHEXE pr list --repo OWNER/REPO --state open`) e **pergunte ao usuário qual mergear** — nunca escolha um PR arbitrário quando há ambiguidade.
- Recupere os detalhes do PR escolhido:
  ```bash
  $GHEXE pr view <N> --repo OWNER/REPO --json number,title,body,headRefName,baseRefName,mergeable,state
  $GHEXE pr diff <N> --repo OWNER/REPO
  $GHEXE pr checks <N> --repo OWNER/REPO
  ```

## Passo 2 — Garantir que a branch local está correta e atualizada

```bash
git fetch origin <headRefName> <baseRefName>
git checkout <headRefName>     # criar tracking branch se não existir localmente
git status                     # nunca sobrescrever trabalho não commitado sem avisar
```

Se houver alterações locais não commitadas e não relacionadas ao PR, pare e avise o usuário antes de continuar.

## Passo 3 — Revisar o diff

Revise `$GHEXE pr diff <N>` com o mesmo rigor de uma code review: bugs de correção, problemas de segurança (OWASP top 10), reuso/simplificação óbvia, quebra de contratos/specs do projeto (`docs/architecture/DECISIONS.md`, `docs/requirements/RF.md`) se o diff tocar área coberta por essas regras. Não é necessário rodar uma auditoria de arquitetura completa — foco em problemas que bloqueariam o merge.

Liste os problemas encontrados (se houver) antes de corrigir, para rastreabilidade.

## Passo 4 — Corrigir problemas encontrados

- Aplique as correções diretamente nos arquivos da branch do PR.
- Se o projeto tiver build/lint/testes para a área alterada (ex: `apps/web` → `npm run build`/`lint`/`test`; `apps/api` → `mvn test`/`./gradlew test`), rode-os para validar a correção antes de commitar.
- Se nenhum problema for encontrado no Passo 3, pule para o Passo 5 sem criar commit vazio.

## Passo 5 — Commitar e dar push

- Commit seguindo o padrão de mensagens já usado no histórico do projeto (`tipo(escopo): descrição`, ex: `fix(rf-15): corrige ...`).
- Só commitar arquivos relacionados à correção (nunca `git add -A`/`git add .` indiscriminado).
- Push para a branch do PR:
  ```bash
  git push origin <headRefName>
  ```

## Passo 6 — Resolver conflitos com a base

```bash
git fetch origin <baseRefName>
git merge origin/<baseRefName> --no-edit
```

- **Sem conflitos:** push do merge commit (se houve) e siga para o Passo 7.
- **Com conflitos:** resolva editando os arquivos em conflito preservando a intenção de ambos os lados (a mudança do PR e o que avançou na base). Depois:
  ```bash
  git add <arquivos resolvidos>
  git commit --no-edit   # finaliza o merge
  git push origin <headRefName>
  ```
- **Pare e pergunte ao usuário** (não tente resolver sozinho) se o conflito:
  - envolve lockfiles/arquivos gerados onde a resolução correta não é óbvia, ou
  - envolve mudanças de lógica de negócio conflitantes onde as duas versões fazem coisas semanticamente diferentes (não é só formatação/import).

## Passo 7 — Verificar checks antes de mergear

```bash
$GHEXE pr checks <N> --repo OWNER/REPO
```

- Se houver checks obrigatórios **falhando**, pare e reporte ao usuário — não force o merge.
- Se não houver checks configurados (comum neste repo) ou todos estiverem passando, siga para o merge.

## Passo 8 — Mergear o PR

Este repositório usa **squash merge** por convenção (commits em `main` no formato `tipo(escopo): descrição (#N)`). Use:

```bash
$GHEXE pr merge <N> --repo OWNER/REPO --squash
```

- Não delete a branch automaticamente (`delete_branch_on_merge` está desabilitado no repo, e a branch local pode estar em uso pelo usuário na sessão atual). Só passe `--delete-branch` se o usuário pedir explicitamente.
- Se `$GHEXE pr merge` falhar com `unable to find git executable in PATH`, use o fallback curl:
  ```bash
  TOKEN=$("$GHEXE" auth token)
  curl -s -X PUT \
    -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
    https://api.github.com/repos/OWNER/REPO/pulls/<N>/merge \
    -d '{"merge_method":"squash"}'
  ```

## Passo 9 — Confirmar resultado

```bash
$GHEXE pr view <N> --repo OWNER/REPO --json state,mergedAt
```

Informe ao usuário: o que foi corrigido (se algo foi), se houve resolução de conflito, e que o PR foi mergeado (com link). Não troque a branch local para `main` nem rode `git pull`/delete automaticamente depois do merge, a menos que o usuário peça.
