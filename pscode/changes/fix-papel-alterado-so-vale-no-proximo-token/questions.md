# Perguntas

- [x] **Encurtar a janela ou aceitá-la?** Fechá-la de verdade: marcar as sessões
  do usuário como obsoletas e responder **401** ao access token antigo. O
  interceptor do front já renova em silêncio e refaz a requisição, então o papel
  novo entra em vigor na hora, sem ninguém ser deslogado.
- [x] **Invalidar o refresh token resolveria?** Não. O access token na mão
  seguiria válido até expirar (janela intacta) e a próxima renovação falharia,
  jogando a pessoa para o login (`axios.ts:57` → `clearToken()`). Descartado.
- [x] **O que entra além da alteração de papel?** Todo ponto que muda o vínculo
  do usuário com a organização — alteração de papel, remoção de membro e uma
  eventual saída por conta própria.
- [x] **E quem está numa tela que o papel novo não alcança?** Nada de especial: o
  `ProtectedRoute` já barra por papel na próxima navegação e a ação proibida
  falha com 403 na tela. Sem código novo no front.
