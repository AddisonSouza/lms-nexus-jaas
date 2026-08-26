## Summary

A tela de criar organização (`/organizations/new`) é hoje um cartão genérico,
sem a identidade visual das outras telas de setup. Esta mudança a reapresenta no
design orgânico — mesmo cabeçalho, ícone e tipografia de `/welcome` — mantendo
os mesmos campos, as mesmas validações e o mesmo fluxo depois de criar.

## Technical detail

- `CreateOrganizationPage.tsx` (hoje um wrapper de uma linha) passa a montar o
  layout: `h1` `font-heading` + subtítulo `text-muted-foreground` fora do card e,
  dentro do `Card elevation="md"`, o círculo `bg-accent-100 text-accent-800` com
  `Building2`, o `CardKicker` e o formulário. Mesma composição de
  `WelcomePage.tsx` e `AcceptInvitePage.tsx`.
- `CreateOrganizationForm.tsx` fica só com campos e submit: perde o `Card` e o
  `<h2>`, ganha `aria-invalid` + `role="alert"` nos erros (padrão já usado em
  `InviteLinkForm.tsx`) e um contador `n/500` sob a descrição, alimentado por
  `watch('description')`.
- Um botão `variant="ghost"` com `render={<Link to="/welcome" />}` abaixo do
  submit dá saída a quem chegou por engano — hoje a única é o logout do
  `MinimalHeader`.
- Só tokens do design system: `--color-accent*`, `--space-*`, `--radius-*`,
  `--shadow-*`, `font-heading`. Sem cor ou raio hardcoded no local de uso; ícones
  só de `lucide-react`.
- Nada muda em `createOrganizationSchema.ts`, `useCreateOrganization.ts` ou
  `organization-api.ts` — mesma validação Zod e mesmo fluxo `POST /organizations`
  → `POST /auth/switch-organization` → `/organizations/{id}`.
- `CreateOrganizationForm.test.tsx` continua cobrindo comportamento (submissão,
  erro 409, validação); só ajustam-se asserções presas ao markup antigo.

## Scope

### In

- Reskin de `/organizations/new` no design orgânico (`CreateOrganizationPage` +
  `CreateOrganizationForm`).
- Ícone, kicker, cabeçalho e texto de apoio sobre o que é uma organização.
- `aria-invalid` + `role="alert"` nos erros e contador de caracteres na descrição.
- Link "Voltar" para `/welcome`.
- Teste de renderização da página e ajuste dos testes do formulário.

### Out

- Backend e contrato de `POST /organizations`.
- Tela de convite / gestão de membros.
- Redesenho de outras telas, do `SetupShell` ou do `MinimalHeader`.
- Novos primitivos em `components/ui/`.


## Subtasks

- [x] Mover o layout da tela para `CreateOrganizationPage`: cabeçalho (h1 + subtítulo), `Card elevation="md"`, círculo com `Building2` e `CardKicker` (#87)
- [x] Enxugar `CreateOrganizationForm` para campos + submit, com `aria-invalid`, `role="alert"` e contador `n/500` na descrição (#89)
- [ ] Adicionar o botão "Voltar" para `/welcome` abaixo do submit (#91)
- [ ] Ajustar `CreateOrganizationForm.test.tsx` e criar `CreateOrganizationPage.test.tsx`; rodar lint, typecheck e a suíte Vitest (#93)
