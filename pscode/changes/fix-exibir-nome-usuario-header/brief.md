# [fix] exibir nome do usuário no lugar do UUID no header

## Objetivo
Hoje o canto superior direito mostra o UUID do usuário logado. Ele deve mostrar o nome.

## Comportamento esperado
- O header mostra o nome do usuário autenticado.
- Sem nome disponível, mostra um fallback legível (ex.: e-mail) — nunca o UUID.

## Fora de escopo
- Mudar o layout do menu de usuário.
- Edição de perfil.
- Avatar/foto.
