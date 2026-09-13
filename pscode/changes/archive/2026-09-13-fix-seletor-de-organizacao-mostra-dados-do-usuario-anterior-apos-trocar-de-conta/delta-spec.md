# Seletor de organização mostra dados do usuário anterior após trocar de conta — Delta

## Changed

- **Sair esvazia o cache do React Query.** O `useLogout` limpava só o
  `authStore`; as chaves de query não dependem do usuário, então a próxima conta
  a entrar lia o cache da anterior (`staleTime` 60s). No seletor, a lista de
  organizações era a antiga e o `organizationId` do novo token não estava nela:
  "Sem organização". Agora o `useLogout` chama `queryClient.clear()` junto do
  `signOut()`, antes de ir a `/login` — também quando o servidor falha.

## Added

- Testes: `useLogout.test.tsx` checa o cache vazio depois do Sair (com e sem
  falha do servidor). `SetupShell.test.tsx` e `MinimalHeader.test.tsx` passam a
  renderizar com `QueryClientProvider`.
- Validado no navegador: conta A (Escola GP77) sai → conta B entra → seletor e
  turmas mostram só dados de B.
- Spec viva `app-layout`: o cenário do Sair limpa também o cache, e novo
  cenário "Outra conta entra depois do Sair".

## Unchanged

- Sessão perdida (interceptor do `axios`, `AuthBootstrap`) — a página recarrega
  e o cache já nasce vazio.
- Chaves de query continuam sem o usuário; o `useLogin` não limpa o cache.
