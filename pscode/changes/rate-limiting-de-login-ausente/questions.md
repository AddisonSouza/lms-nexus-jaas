# Grill Me
- [x] Quais endpoints entram no bloqueio? — Todo `/auth/**` (conforme SEC-08).
- [x] De onde vem o IP do cliente? — Endereço remoto da conexão; `X-Forwarded-For` só se `quarkus.http.proxy` for ligado por config (não há proxy reverso hoje).
- [x] O que conta como falha e o que acontece durante o bloqueio? — Só 401 (422 de validação não conta); bloqueado → 429 antes de checar credenciais, mesmo com senha correta.
- [x] Como a tela de login mostra o bloqueio? — "Muitas tentativas. Tente novamente em N minutos." a partir do `Retry-After`, com botão Entrar desabilitado até o prazo.
- [x] Em `/auth/**`, o que soma no contador e o que o bloqueio barra? — Somam 401 do login e 400 de token inválido no reset-password; refresh/logout/switch não somam. Bloqueio → 429 em todo `/auth/**`, exceto refresh e logout.
