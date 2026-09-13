# Seletor de organização mostra dados do usuário anterior após trocar de conta

## Summary

Quem sai de uma conta e entra com outra vê, no seletor de organização, as
organizações da conta anterior. Esta mudança faz o Sair apagar os dados
guardados da sessão, para a nova conta começar do zero.

## Technical detail

- **Causa:** o `useLogout` chama `signOut()` e navega para `/login`, mas não
  limpa o cache do React Query (`staleTime` 60s em `lib/query-client.ts`). As
  chaves não dependem do usuário (`organizationKeys.lists()`), então a próxima
  conta lê a lista antiga; o `OrganizationSwitcher` não acha o `organizationId`
  do novo token e mostra "Sem organização".
- **Correção:** o `useLogout` passa a usar `useQueryClient()` e chamar
  `queryClient.clear()` junto do `signOut()`, antes do `navigate('/login')` —
  mesmo passo de `useSwitchOrganization` e `useAcceptInvitation`. Vale também
  quando a chamada ao servidor falha.
- **Testes:** `useLogout.test.tsx` passa a renderizar com `QueryClientProvider`
  e checa que o cache fica vazio. Testes que usam o `useLogout` real (ex.:
  `SetupShell.test.tsx`) precisam do provider.
- **Continua igual:** sessão perdida (interceptor do `axios` recarrega a
  página; `AuthBootstrap` roda num carregamento novo) — o cache já nasce vazio.

## Scope

### In

- `queryClient.clear()` no `useLogout` + testes.
- Validação no navegador do ciclo conta A → Sair → conta B.

### Out

- Mudanças visuais no seletor e no fluxo de criar organização.
- Chaves de query por usuário; limpar o cache no `useLogin`.

## Subtasks

- [ ] FE: `useLogout` limpa o cache do React Query ao sair (também se o servidor falhar) + testes
- [ ] Validar no navegador: conta A sai → conta B entra → seletor e turmas mostram só dados de B
