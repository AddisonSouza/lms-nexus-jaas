# [feat] confirmação de senha e validação de senha forte no cadastro

## Objetivo
Reduzir erros de digitação e senhas fracas no cadastro de usuário, exigindo a
confirmação da senha e uma senha que atenda a critérios mínimos de força.

## Comportamento esperado
- O formulário de cadastro ganha um campo "Confirmar senha".
- O envio é bloqueado enquanto os dois campos não forem idênticos.
- A senha passa a ser validada contra critérios de força (Zod), com a mensagem
  de erro indicando o que falta.
- Os critérios exatos (tamanho mínimo, maiúscula/minúscula, número, símbolo)
  são definidos no refine.

## Fora de escopo
- Alteração de senha de usuário já cadastrado.
- Redefinição de senha (reset).
- Medidor visual de força.
- Política de senha configurável por organização.
- Mudanças na validação do back-end.
