# Questions — #210

- [x] Onde corrigir? → **Card próprio**, fora do #204 (regressão do #198).
- [x] Como a correção funciona? → A tela de aceite só manda para
  `/login?invite=` quem **não saiu por vontade própria**. Quem estava logado e
  clicou em Sair vai para `/login` limpo; o #198 continua valendo para quem
  chega deslogado.
- [x] E se a sessão expirar sozinha na tela de aceite (refresh falha)? →
  **Mantém o convite**: a pessoa não escolheu sair e volta a
  `/login?invite=<token>` para retomar o aceite. Só o Sair explícito limpa.
