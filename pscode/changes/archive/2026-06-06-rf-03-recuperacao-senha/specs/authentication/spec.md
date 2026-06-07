## MODIFIED Requirements

### Requirement: Invalidação de Refresh Tokens ao resetar senha

Extensão do comportamento de `RefreshTokenRepository`: ao concluir o reset de senha, todos os Refresh Tokens ativos do usuário são invalidados (SEC-03).

#### Scenario: Reset concluído com sessões ativas
- **WHEN** `ResetPasswordService.execute()` conclui com sucesso
- **THEN** `RefreshTokenRepository.deleteAllByUserId(userId)` invocado — todas as entradas `rt:{token}` do usuário removidas do Redis

#### Scenario: Usuário sem sessões ativas
- **WHEN** reset concluído e usuário não possui Refresh Tokens ativos
- **THEN** `deleteAllByUserId` executa sem erro (no-op)
