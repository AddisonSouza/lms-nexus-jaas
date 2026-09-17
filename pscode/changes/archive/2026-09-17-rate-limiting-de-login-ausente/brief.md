# Rate limiting de login ausente

Card #267 · Severidade crítica · RF-02, SEC-08 · origem: bateria E2E 15/09/2026

## Objetivo
Implementar o bloqueio por IP previsto em SEC-08. Hoje não há limite: sete
tentativas erradas seguidas responderam 401 e a oitava, correta, entrou.

## Comportamento esperado
- 5 falhas de login em 1 minuto vindas do mesmo IP → IP bloqueado por 15 minutos.
- Contadores ficam no Redis (com TTL), não no banco.
- Durante o bloqueio, `POST /auth/login` responde 429 com header `Retry-After`.
- A tela de login mostra uma mensagem clara de bloqueio.
- Login bem-sucedido zera o contador do IP.

## Fora do escopo
- Bloqueio por conta (por e-mail).
- CAPTCHA.
