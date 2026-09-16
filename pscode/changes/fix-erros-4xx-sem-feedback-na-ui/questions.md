# Grill Me

- [x] Helper compartilhado ou tratamento caso a caso? → Helper
  `apiErrorMessage(error)` em `apps/web/src/lib` (features não se importam).
- [x] Onde mostrar erro nos diálogos de confirmação? → `ConfirmDialog` ganha
  prop `error?: string | null` e permanece aberto na falha.
