# fix: confirmação de e-mail não idempotente — Delta

## Changed

- **Confirmar o e-mail duas vezes deixa de ser erro (RF-04, `REQ-EMAILCONF-02`).**
  `ConfirmEmailService` apagava a chave `ect:{token}` logo após ativar a conta,
  então o segundo clique no link não resolvia mais usuário nenhum e caía em
  `InvalidConfirmationTokenException` — 400, "Link inválido ou expirado", como se
  a confirmação não tivesse valido. O ramo `EmailAlreadyConfirmedException` (409)
  logo abaixo era inalcançável. O token não é mais apagado: expira sozinho pelo
  TTL de 24h que `RegisterUserService` grava. `GET /auth/confirm-email` com token
  já usado agora responde **409 `EMAIL_ALREADY_CONFIRMED`**.
- **`ConfirmEmailCallbackPage` trata o 409 como sucesso.** Antes mostrava
  `XCircle` com "E-mail já confirmado" e parava ali. Agora renderiza o card de
  sucesso (`CheckCircle`, "Sua conta já está ativa. Redirecionando para o
  login...") e navega para `/login?confirmed=true` após 2s — o mesmo desfecho do
  primeiro clique. O card de erro fica só para o token de fato inválido, e volta
  a ter o formulário de reenvio sem condicional.
- **Token de confirmação deixa de ser de uso único.** Dentro das 24h ele pode ser
  reapresentado. A troca é deliberada: o token não é credencial — a única ação
  que habilita é `activate()`, já guardada pelo status, e `PENDING_CONFIRMATION`
  só é atribuído em `RegisterUserService`, nunca reatribuído. O replay é inócuo.

## Added

- **IT do duplo clique** em `ConfirmEmailResourceIT`: registra, extrai o token do
  corpo do e-mail, confirma (204) e confirma de novo (409
  `EMAIL_ALREADY_CONFIRMED`). Verificado nos dois sentidos — repondo o
  `invalidate`, o segundo clique volta a devolver 400.

## Removed

- **`confirmationTokenRepository.invalidate(token)`** de `ConfirmEmailService` —
  era a única chamada em produção.

## Conhecido, fora deste card

- **`invalidate` ficou sem chamador**: o método segue na porta
  `EmailConfirmationTokenRepository` e no adapter `EmailConfirmationRedisRepository`.
  Código morto hoje; remover ficou fora do escopo.
- **Link expirado com conta já ativa** continua 400 "Link inválido ou expirado"
  com botão de reenvio — passadas as 24h a chave some e não há como distinguir
  do token inexistente.
- A asserção `getByRole('link', 'Voltar ao login')` saiu do teste do 409: o card
  de sucesso não traz esse link, porque redireciona sozinho.
