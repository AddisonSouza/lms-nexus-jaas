# Tela de criar organização no design orgânico — Delta

## Added

- **`CreateOrganizationPage` monta o layout da tela**: `h1` `font-heading` com
  subtítulo `text-muted-foreground` fora do card e, dentro do
  `Card elevation="md"`, o círculo `bg-accent-100 text-accent-800` com `Building2`
  e o `CardKicker` — mesma composição de `WelcomePage` e `AcceptInvitePage`.
- **Saída da tela**: botão `variant="ghost"` renderizado como `<Link to="/welcome">`
  abaixo do submit. Até então a única saída era o logout do `MinimalHeader`.
- **Acessibilidade dos campos**: `aria-invalid` no nome e na descrição, e
  `role="alert"` nas três mensagens de erro (nome, descrição e erro da API),
  seguindo o padrão de `InviteLinkForm`.
- **Contador de caracteres** `n/500` sob a descrição, alimentado por
  `watch('description')`.
- **`CreateOrganizationPage.test.tsx`**: cobre o cabeçalho, o kicker, os campos e
  o "Voltar" apontando para `/welcome`.

## Changed

- **`CreateOrganizationForm`** deixa de ser a tela inteira e passa a ser só
  campos + submit: perde o `Card` e o `<h2>`, que migraram para a página.
- **`CreateOrganizationForm.test.tsx`**: o teste do 409 passa a asserir pelo
  `role="alert"`; somam-se testes do `aria-invalid` e do contador.

## Notes

- Nenhuma mudança em `createOrganizationSchema.ts`, `useCreateOrganization.ts` ou
  `organization-api.ts` — mesma validação Zod e mesmo fluxo `POST /organizations`
  → `POST /auth/switch-organization` → `/organizations/{id}`.
- `DESCRIPTION_MAX_LENGTH` vive no componente e espelha o `max(500)` do schema,
  que o refine pediu para não tocar.
- Sem estilo visual associado ao `aria-invalid` nos primitivos `Input`/`Textarea`
  — só o `Button` trata esse atributo hoje. Quem sinaliza o erro é a mensagem.
- O seletor de organização da sidebar, a tela de Membros e o link aberto de
  convite de organização ficaram fora: viraram o card #104 e decisões pendentes.
