# Botão para ver senha nos campos de senha

## Objetivo

Permitir que o usuário alterne a visibilidade do que digitou nos campos de
senha, para conferir o valor antes de enviar.

## Comportamento esperado

- Cada campo de senha ganha um botão (ícone de olho, Lucide React) dentro do
  input, que alterna entre texto oculto e visível.
- Aplica-se aos campos de senha das telas de autenticação: login, cadastro e
  redefinição de senha.
- O padrão é oculto.
- Alternar um campo não afeta os demais.

## Fora de escopo

- Medidor de força de senha.
- Regras de validação de senha.
- Mudanças no fluxo de autenticação.
- Campos de senha fora das telas de auth.
