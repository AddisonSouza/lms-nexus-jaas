# Perguntas — refinamento

- [x] **Quais critérios de força a senha deve exigir?**
  Mínimo 8 caracteres com maiúscula, minúscula, número e símbolo (4 classes).
  Mantém o mínimo de 8 já cravado no back-end e na spec `user-registration`.
- [x] **Como o erro aparece no formulário?**
  Uma única mensagem montada com os critérios não atendidos
  ("A senha precisa de: uma maiúscula, um número").
- [x] **Onde a regra mora e o reset adota agora?**
  `passwordSchema` compartilhado em `features/auth/schemas/`, consumido só pelo
  `registerSchema`. `resetPasswordSchema` fica como está (min 8).
