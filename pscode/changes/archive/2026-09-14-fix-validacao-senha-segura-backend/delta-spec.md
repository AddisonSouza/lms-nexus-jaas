# [fix] validação de senha segura no backend — Delta

## Changed

- **`POST /auth/register` exige senha segura na própria API.** Antes aceitava
  quaisquer 8 caracteres quando chamada direto; agora a senha precisa de mínimo
  8 caracteres, maiúscula, minúscula, número e símbolo (mesma regra do front).
  Senha fraca → `422 { "errors": [...] }` listando só os critérios que faltam,
  sem criar usuário nem enviar e-mail.
- **`POST /auth/reset-password` aplica a mesma regra à nova senha.** Antes
  bastava `≥ 8 chars`; agora senha fraca → `422`, validado antes do token.
- **`API_CONTRACT.md`**: cadastro documenta `422` (era `400`) para erro de
  validação; reset ganha a linha `422`.

## Added

- **Constraint `@StrongPassword`** (`identity/interfaces/rest/validation`),
  reutilizável em qualquer DTO. Chegou à `main` junto do commit `1e4b947` (#259).
