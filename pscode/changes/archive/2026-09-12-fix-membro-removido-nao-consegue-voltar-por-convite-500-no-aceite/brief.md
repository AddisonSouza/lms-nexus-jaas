# Membro removido não consegue voltar por convite (500 no aceite)

## Objetivo

Um membro removido da organização deve poder voltar aceitando um novo convite,
com o papel desse convite.

## Comportamento esperado

- O admin remove um membro, convida o mesmo e-mail de novo, e o convidado aceita:
  ele volta a ser membro ativo com o papel do convite novo.
- Nenhuma falha de banco chega ao usuário como `500`.

## Comportamento atual (causa)

A remoção é soft delete (`organization_members.deleted_at`), então a linha fica
no banco. O `AcceptInviteService` só procura vínculo **ativo**
(`existsActiveByOrgAndUser`, com `deleted_at IS NULL`), não encontra e manda o
repositório salvar um vínculo novo (id novo → insert). A restrição
`uq_member UNIQUE (organization_id, user_id)` (V005) ignora o `deleted_at` e
responde `Duplicate entry`; a transação é desfeita e o convite continua
`PENDING`.

Reproduzido no teste manual do #198 (log da API, 12/09: remoção 20:13:35,
convite 20:14:01, seis aceites com erro entre 20:15:59 e 20:16:35).

## A decidir no refine

- Reativar o vínculo removido ou mudar a restrição.
- Se turmas (`uq_classroom_member`, também com soft delete) têm o mesmo problema.

## Fora de escopo

- Login e redirecionamento do convite (#198).
- Regras de reenvio e cancelamento de convite (#77).
