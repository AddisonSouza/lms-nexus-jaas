# Grill Me
- [x] Onde a regra de senha segura mora no back-end? — Anotação Bean Validation `@StrongPassword` reutilizável nos DTOs de request.
- [x] Qual status HTTP para senha fraca? — `422`, padrão atual do `ValidationExceptionMapper` (`{ "errors": [...] }`).
- [x] Qual mensagem de erro? — Igual ao front: "Senha deve ter no mínimo 8 caracteres" ou "A senha precisa de: ..." listando só os critérios que faltam.
