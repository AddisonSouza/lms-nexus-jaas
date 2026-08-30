# Revisão dos convites de organização e turma

## Objetivo

Verificar ponta a ponta se os fluxos de convite funcionam e corrigir o que
estiver quebrado.

- **Convite de organização:** geração do link/token (`InvitationResource`) até o
  aceite no front (`AcceptInvitePage`, `useAcceptInvitation`).
- **Entrada em turma:** geração do código de convite e o join
  (`JoinClassroomForm` → `ClassroomResource`).

## Comportamento esperado

O convidado entra na organização com o papel correto, o aluno entra na turma
pelo código, e os casos de erro (token expirado/já usado, código inválido,
usuário já membro) devolvem mensagem clara.

## Fora de escopo

- Novos canais de convite (e-mail em massa, SMS).
- Redesign das telas.
- Convite no nível de disciplina (`Subject`): se não existir hoje, será apenas
  reportado na revisão, não implementado aqui.
