# Confirmação de senha e validação de senha forte no cadastro — Delta

## Added

- **`passwordSchema`** (`apps/web/src/features/auth/schemas/passwordSchema.ts`) é
  a regra única de força de senha do front: mínimo 8 caracteres **com**
  maiúscula, minúscula, número e símbolo. Um `superRefine` monta **uma** mensagem
  listando só os critérios **não atendidos** ("A senha precisa de: uma maiúscula,
  um número"), em vez de despejar quatro erros de uma vez. O comprimento continua
  com a mensagem própria do `.min()` — é o primeiro issue do Zod, então é o que o
  react-hook-form exibe quando a senha é curta.
- **Campo "Confirmar senha" no cadastro.** `RegisterForm.tsx` ganha um segundo
  `PasswordInput` com `autoComplete="new-password"`, logo abaixo da senha,
  seguindo o mesmo bloco (`label htmlFor` + `<p className="text-xs
  text-destructive">`) dos campos existentes.
- Testes: `passwordSchema.test.ts` (cada critério isolado, a mensagem composta,
  a senha válida) e `RegisterForm.test.tsx` (senha fraca, senhas divergentes,
  caminho feliz) — 11 casos novos.

## Changed

- **`registerSchema` passou a ser um `ZodEffects`**: consome o `passwordSchema`
  em `password`, ganha `confirmPassword: z.string()` e um `.refine` comparando os
  dois com `path: ['confirmPassword']` e a mensagem "As senhas não conferem" — o
  mesmo padrão já usado no `resetPasswordSchema`. `RegisterFormData` agora inclui
  `confirmPassword`.
- **Spec viva `user-registration`**: novo requirement "Registration form enforces
  a strong, confirmed password" com quatro cenários (critérios faltantes,
  comprimento, confirmação divergente, confirmação não vaza para o servidor). O
  cenário "Successful registration" passou a falar em senha forte confirmada em
  um segundo campo.

## Unchanged

- **O back-end não mudou.** `RegisterRequest` mantém `@Size(min = 8)`; o front
  ficou mais estrito que o servidor, o que não quebra o contrato.
- **`confirmPassword` não chega à API.** `registerUser` já montava o payload
  campo a campo (`fullName`, `email`, `password`), então o campo novo fica no
  formulário.
- `resetPasswordSchema` e a tela de redefinição continuam com o mínimo de 8 sem
  critérios de força — adotar o `passwordSchema` ali ficou fora de escopo.

## Reported, not fixed

- **O serviço `api` do `infra/docker-compose.yml` não sobe**: o
  `infra/docker/api/Dockerfile.dev` referencia
  `quarkus/quarkus-devtools-ubi-quarkus-native-image:3.12.3-java21`, imagem que
  não existe mais no Docker Hub. A validação foi feita com `mvn quarkus:dev`.
  Merece card próprio.
- Medidor visual de força, checklist reativo abaixo do campo e política de senha
  por organização seguem fora de escopo.
