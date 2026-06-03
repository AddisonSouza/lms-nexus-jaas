---
name: "PS: Propose"
description: Propose a new change - create it and generate all artifacts in one step
category: Workflow
tags: [workflow, artifacts, propose]
---

## Asking the user

When this workflow needs a decision or confirmation from the user, prefer the
`AskUserQuestion` tool over a free-text question:

- Use `AskUserQuestion` for any decision or confirmation (e.g. "Which approach?",
  "Can I open the PR?", "Move the card to Ready to Dev?"). Present 2–4 concrete,
  mutually exclusive options.
- Always keep the embedded free-text answer ("Other") available — never remove
  it. The user can always type a custom response.
- Fall back to a plain free-text question only when there are no reasonable
  options to offer, or when `AskUserQuestion` is unavailable.
- Do NOT use `AskUserQuestion` for progress updates or status messages — only
  for genuine questions that need the user's input.

Propose a new change - create the change and generate all artifacts in one step.

I'll create a change with artifacts:
- proposal.md (what & why)
- design.md (how)
- tasks.md (implementation steps)

After artifacts are created, a **refinement validation loop** runs: the GitHub Issue is updated with the refined plan, the user reviews it, gives feedback, and when satisfied the GitHub Projects status is moved to accepted.

When ready to implement, run /ps:apply

---

**Input**: The user's request should include a change name (kebab-case) OR a description of what they want to build.

**Steps**

1. **If no clear input provided, ask what they want to build**

   Use the **AskUserQuestion tool** (open-ended, no preset options) to ask:
   > "What change do you want to work on? Describe what you want to build or fix."

   From their description, derive a kebab-case name (e.g., "add user authentication" → `add-user-auth`).

   **IMPORTANT**: Do NOT proceed without understanding what the user wants to build.

1b. **Fase de grill — interrogue o plano antes de gerar artefatos**

   Antes de criar a change e gerar os artefatos, conduza a **fase de grill** para garantir que a proposta reflita o que realmente deve existir — não apenas a descrição inicial. Aplique o mesmo comportamento da skill `grill-me` (`/ps:grill-me`):

   - Faça **uma pergunta por vez** — nunca despeje várias perguntas juntas. Aguarde a resposta antes da próxima.
   - Acompanhe **cada** pergunta com a **sua resposta recomendada** e um motivo curto. Quando houver opções discretas, use a **ferramenta AskUserQuestion** com a recomendação como primeira opção ("(Recomendada)").
   - **Explore o código quando há evidência**: se uma pergunta pode ser respondida pelo próprio repositório (convenções, padrões, features semelhantes), investigue o código em vez de perguntar. Só pergunte ao usuário decisões de produto, prioridades, trade-offs e intenção.
   - Navegue a árvore de decisão progressivamente, resolvendo dependências; não repergunte o que já foi respondido pelo código ou por uma resposta anterior.
   - Encerre quando houver **entendimento compartilhado** e apresente um resumo curto (o que será construído, decisões com motivos, fora de escopo) antes de seguir.

   Os artefatos gerados a partir do Passo 2 SHALL refletir esse entendimento refinado.

1c. **PR Integration — abrir PR draft no início (opcional)**

   Use the **Read tool** (NOT a shell command) to read `pscode/config.yaml` from the current working directory.
   If the Read tool returns an error (file not found), or `pr.enabled` is not `true`, **skip this step entirely** — no PR, no branch, no checkpoint commits — and continue to Step 2. Opening the PR is then left to `/ps:apply`. Set `PR_OPENED = false`.

   **If `pscode/config.yaml` exists and `pr.enabled: true`:**

   Ask **once**, using the **AskUserQuestion tool**, whether to open the draft PR now:
   > "Quer abrir o Pull Request em DRAFT agora? O PR nasce em draft e cresce junto com o refinamento."
   > - ✅ Sim, abrir o PR draft agora (Recomendada)
   > - ❌ Não, deixar para o apply

   **If the user declines (Não):** continue the normal flow from Step 2 without any PR steps. Do NOT create a branch or commit automatically. The PR will be opened later by `/ps:apply`. Set `PR_OPENED = false`.

   **If the user accepts (Sim):** proceed **without asking for any further authorization**:
   1. Resolve the branch name from `pr.branch.pattern`, substituting `{change-name}` with the change name, `{type}` with feat/fix/chore (infer from the change; default `feat`), and `{ticket}` with the ticket ID if available.
   2. Create and switch to the branch: `git checkout -b <branch>`.
   3. Create the change scaffold (this is Step 2): `pscode new change "<name>"`.
   4. Stage and commit the scaffold: `git add -A && git commit -m "chore(<name>): scaffold change"`.
   5. Push and set upstream: `git push -u origin <branch>`.
   6. Open the PR in **DRAFT**, deriving the title from `pr.title.template` and the body from `pr.description.template` (substitute `{change-name}`/`{type}`/`{ticket}`).

      **Referência da task no corpo (GitHub Issue):** if `pr.taskLinkInDescription` is not `false` (default ON when the field is absent) **and** a `ghItemId` is available (resolved in Step 3), prefix the resolved body with a `Task: https://github.com/<repo>/issues/<issueNumber>` line followed by a blank line, before the `pr.description.template` content. **Skip gracefully** when `pr.taskLinkInDescription: false` or there is no `ghItemId` — open the PR normally without the line, never block. If `ghItemId` is only resolved after this step, the line can be added by editing the PR body right after Step 3.

      `gh pr create --draft --title "<resolved title>" --body "<resolved body>"`.
   7. Capture the PR URL from the `gh` output, save it as `prUrl`, and set `PR_OPENED = true`.

   **Comentário do link no tracker:** after the PR is opened, if `pr.comments.linkInTask: true` and `issueNumber` is not null, comment the PR link on the GitHub Issue:
   ```bash
   "<ghConfig.gh>" issue comment <issueNumber> --repo <ghConfig.repo> --body "🔀 Pull Request (DRAFT) aberto: <prUrl>"
   ```
   The `ghItemId` is resolved in Step 3 — if not yet available, post this comment right after Step 3 instead.

   **Tratamento de falha (não-bloqueante):** if `gh` or `git` fails — `gh` not installed, not authenticated, or no GitHub remote — **do NOT block**:
   - Clearly state what failed and how to fix it (e.g., "instale o `gh` CLI", "rode `gh auth login`", "configure um remote GitHub").
   - Ask whether the user wants the agent to resolve it in parallel (e.g., run `gh auth login`).
   - **Continue the propose flow regardless.** Any branch already created and local commits are preserved; set `PR_OPENED = true` only if the PR was actually opened.

2. **Create the change directory**
   ```bash
   pscode new change "<name>"
   ```
   This creates a scaffolded change in the planning home resolved by the CLI with `.pscode.yaml`.

   **If you already created the change scaffold in Step 1c** (PR accepted), skip this step — the change directory already exists.

3. **GitHub Projects Integration (optional)**

   Use the **Read tool** (NOT a shell command) to read `pscode/github.yaml` from the current working directory.
   If the Read tool returns an error (file not found), skip all GitHub Projects steps and continue to Step 4.

   Otherwise, parse the YAML and extract:
   - `repo` → e.g. `AddisonSouza/lms-nexus-jaas` (owner = first component before `/`)
   - `project` → project number (integer)
   - `projectNodeId` → GitHub Projects GraphQL node ID
   - `statusFieldId` → Status field ID
   - `statuses.proposed` → status option ID
   - `statuses.accepted` → status option ID
   - `gh` → path to gh CLI (default: `gh` if field absent)

   Save all fields as `ghConfig`. Set `ghItemId = null`.

   **3a. Extract issue number from change name**

   Apply to the change name (e.g. `rf-01-cadastro-usuario`):
   - Pattern `rf-NN` or `rf-NNN` → extract N as integer (e.g. `rf-01` → `1`)
   - Pattern `issue-NN` → extract N as integer
   - If no pattern matches → `issueNumber = null`

   **3b. Find the GitHub Projects item (if issueNumber is not null)**

   ```bash
   "<ghConfig.gh>" project item-list <ghConfig.project> --owner "<owner>" --format json
   ```

   Parse the output to find the item where `content.number == issueNumber` (type Issue).
   Save the item's `id` field as `ghItemId`.

   If no item found or the command fails → `ghItemId = null`, log and continue (non-blocking).

   **3c. Update status to "proposed" (if ghItemId is not null)**

   ```bash
   "<ghConfig.gh>" project item-edit --id <ghItemId> --field-id <ghConfig.statusFieldId> --project-id <ghConfig.projectNodeId> --single-select-option-id <ghConfig.statuses.proposed>
   ```

   If this call fails, log the error and continue — GitHub Projects is auxiliary, never blocking.

   Save `ghItemId` for use in the refinement loop steps.

4. **Get the artifact build order**
   ```bash
   pscode status --change "<name>" --json
   ```
   Parse the JSON to get:
   - `applyRequires`: array of artifact IDs needed before implementation
   - `artifacts`: list of all artifacts with their status and dependencies
   - `planningHome`, `changeRoot`, `artifactPaths`, and `actionContext`: path and scope context

5. **Create artifacts in sequence until apply-ready**

   Use the **TodoWrite tool** to track progress through the artifacts.

   Loop through artifacts in dependency order:

   a. **For each artifact that is `ready` (dependencies satisfied)**:
      - Get instructions:
        ```bash
        pscode instructions <artifact-id> --change "<name>" --json
        ```
      - The instructions JSON includes:
        - `context`: Project background (constraints for you - do NOT include in output)
        - `rules`: Artifact-specific rules (constraints for you - do NOT include in output)
        - `template`: The structure to use for your output file
        - `instruction`: Schema-specific guidance for this artifact type
        - `resolvedOutputPath`: Resolved path or pattern to write the artifact
        - `dependencies`: Completed artifacts to read for context
      - Read any completed dependency files for context
      - Create the artifact file using `template` as the structure and write it to `resolvedOutputPath`
      - Apply `context` and `rules` as constraints — do NOT copy them into the file
      - Show brief progress: "Created <artifact-id>"

   b. **Continue until all `applyRequires` artifacts are complete**
      - After creating each artifact, re-run `pscode status --change "<name>" --json`
      - Check if every artifact ID in `applyRequires` has `status: "done"`
      - Stop when all `applyRequires` artifacts are done

   c. **If an artifact requires user input** (unclear context):
      - Use **AskUserQuestion tool** to clarify
      - Then continue with creation

6. **Show final status**
   ```bash
   pscode status --change "<name>"
   ```

7. **Checkpoint commit — após gerar os artefatos (only if `PR_OPENED = true`)**

   If a draft PR was opened in Step 1c, commit and push the generated artifacts as a checkpoint so the PR reflects the refined plan:
   ```bash
   git add -A && git commit -m "docs(<name>): add planning artifacts" && git push
   ```
   If `PR_OPENED = false`, skip — no automatic commits. Failures here are non-blocking (same handling as Step 1c).

---

## Refinement Validation Loop

After all artifacts are created, enter the **refinement validation loop**. This loop runs until the user approves the plan or explicitly cancels.

### Step R1 — Show Refinement Summary

Present the following structured summary to the user. Read `proposal.md`, `design.md`, and `tasks.md` from the change directory to extract the relevant information.

```markdown
## 🔍 Refinamento da Proposta — <name>

**Objetivo:** <1-2 sentences summarizing what will be built, from proposal.md>

### O que será implementado
<3-5 bullet points extracted from design.md / tasks.md describing the main implementation steps>

### Escopo e decisões técnicas
<2-3 key technical decisions or constraints from design.md>

### Tarefas geradas
<numbered list of tasks from tasks.md (brief, one line each)>

---
Para iniciar a implementação quando aprovado:
```
/ps:apply <name>
```
```

---

### Step R1b — Update GitHub Projects item (before asking for approval)

Update the GitHub Issue with the refinement content **before** asking for approval,
so the user can use the issue as a visual reference when deciding.

1. **Add a refinement comment to the GitHub Issue** (if `ghItemId` is not null and `issueNumber` is not null):
   Build the comment from the artifacts already read in Step R1:
   ```bash
   "<ghConfig.gh>" issue comment <issueNumber> --repo <ghConfig.repo> --body "## Proposta refinada ✓

   **Change:** \`<name>\`
   **Artefatos gerados:** proposal.md · design.md · tasks.md

   ### Resumo
   <2-3 line summary of what will be built>

   **O que será implementado:**
   <bullet list from design.md / tasks.md>

   **Decisões técnicas:**
   <key decisions from design.md>

   **Artefatos:** pscode/changes/<name>/

   _Aguardando aprovação para mover para accepted._"
   ```

If the gh call fails, continue — GitHub Projects is auxiliary, never blocking.

---

### Step R2 — Ask for user approval

Use **AskUserQuestion** to ask:

> "A implementação e o planejamento estão de acordo com o esperado?"

Options:
- ✅ Sim, está refinada — mover para Ready to Dev
- 🔄 Não, quero ajustar o plano
- ❌ Cancelar (manter em refinamento)

At this point the GitHub Issue already reflects the current refinement (Step R1b),
so the user can review it before deciding.

---

### Step R2a — If APPROVED (Sim, está refinada)

The refinement comment was already added to the GitHub Issue in Step R1b.
Now update the GitHub Projects status to "accepted" and register the approval.

1. **Update GitHub Projects status to "accepted"** (if `ghItemId` is not null):
   ```bash
   "<ghConfig.gh>" project item-edit --id <ghItemId> --field-id <ghConfig.statusFieldId> --project-id <ghConfig.projectNodeId> --single-select-option-id <ghConfig.statuses.accepted>
   ```

2. **Add a final approval comment to the GitHub Issue** (if `issueNumber` is not null):
   ```bash
   "<ghConfig.gh>" issue comment <issueNumber> --repo <ghConfig.repo> --body "## ✅ Aprovado para desenvolvimento

   O planejamento foi revisado e aprovado.

   ## Próximo passo

   Para implementar as tasks da change, rode:

   \`\`\`
   /ps:apply \"<name>\"
   \`\`\`"
   ```

3. **Show success message:**
   ```markdown
   ## ✅ Pronto para desenvolvimento!

   **Change:** <name>
   **GitHub Projects:** Status atualizado para accepted

   Quando quiser iniciar a implementação:
   ```
   /ps:apply <name>
   ```
   ```

---

### Step R2b — If NOT APPROVED (Quero ajustar o plano)

1. **Ask what needs to change** using **AskUserQuestion**:
   > "O que você gostaria de ajustar no plano? Descreva as mudanças necessárias."

2. **Apply the requested changes** to the relevant artifacts:
   - Changes to scope or requirements → update `proposal.md`
   - Changes to technical approach → update `design.md`
   - Changes to tasks → update `tasks.md`

3. **Checkpoint commit — após o ajuste (only if `PR_OPENED = true`)**:
   If a draft PR was opened in Step 1c, commit and push the adjusted artifacts as a checkpoint:
   ```bash
   git add -A && git commit -m "docs(<name>): refine plan" && git push
   ```
   If `PR_OPENED = false`, skip. Failures here are non-blocking (same handling as Step 1c).

4. **Go back to Step R1** and show the updated refinement summary, then **re-run Step R1b**
   so the GitHub Issue comment reflects the adjusted plan before asking again.
   Keep looping until the user approves or cancels.

---

### Step R2c — If CANCELLED

Show:
```markdown
## ⏸ Refinamento pausado

O status no GitHub Projects permanece em **proposed**.
Retome o refinamento quando quiser com `/ps:explore <name>`.
```

Do NOT move the card. Stop the loop.

---

**Artifact Creation Guidelines**

- Follow the `instruction` field from `pscode instructions` for each artifact type
- Read dependency artifacts for context before creating new ones
- Use `template` as the structure — fill in its sections
- **IMPORTANT**: `context` and `rules` are constraints for YOU, not content for the file

**Guardrails**
- Create ALL artifacts needed for implementation (as defined by schema's `apply.requires`)
- Always read dependency artifacts before creating a new one
- If context is critically unclear, ask the user — but prefer reasonable decisions to keep momentum
- If a change with that name already exists, ask if user wants to continue it or create a new one
- Verify each artifact file exists after writing before proceeding to next
- If gh CLI calls fail, continue normally — GitHub Projects is auxiliary, not blocking
- All content written to GitHub Issues must be in Portuguese
- **The refinement loop is mandatory** — never skip it even if GitHub Projects is not configured; the approval question must always be asked
- **Preserve the loop** — do not exit until the user explicitly approves (moves to Ready to Dev) or cancels

