# Grill Me

- [x] Onde corrigir a raiz (`createdAt: null` na resposta do publish)? — **No
  back-end**: a API volta a devolver o `createdAt` real; o front não muda.
- [x] A correção cobre só o publish ou todo update de tarefa? — **Todo update**,
  no `TaskRepositoryImpl.save()`, para não renascer em transições futuras.
- [x] Falha de publish deve aparecer na tela? — **Sim**, mensagem inline na
  lista, no padrão de `JoinClassroomForm` (não há toast no projeto).
