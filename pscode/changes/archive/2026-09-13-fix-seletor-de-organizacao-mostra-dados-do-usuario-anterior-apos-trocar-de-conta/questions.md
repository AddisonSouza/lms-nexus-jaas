# Grill Me

- [x] Quando limpar o cache do React Query? → **No Sair** (`useLogout`). Sessão
  perdida já recarrega a página (interceptor do `axios`, `AuthBootstrap`), então
  o Sair é o único caminho de troca de conta sem recarga.
- [x] O que limpar? → **Todo o cache** (`queryClient.clear()`), como já fazem a
  troca de organização e o aceite de convite.
- [x] O brief está ok? → Sim.
