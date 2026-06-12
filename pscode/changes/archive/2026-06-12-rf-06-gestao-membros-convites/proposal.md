## Why

Com o RF-05, organizações podem ser criadas. O próximo passo natural do MVP é permitir que o `ADMIN_ORG` adicione outros usuários à organização, definindo seus papéis. Sem essa funcionalidade, uma organização existe mas não tem equipe — tornando o produto inutilizável.

## What Changes

- Novo fluxo de **convite por e-mail**: `ADMIN_ORG` envia convite com papel (GESTOR, PROFESSOR ou ALUNO); destinatário recebe link com token UUID válido por 7 dias
- Novo endpoint **aceitar convite**: `POST /invitations/{token}/accept` — usuário autenticado aceita e é inserido em `organization_members`; sem conta → redirecionado para `/register?invite=TOKEN`
- Novo endpoint **remover membro**: `DELETE /organizations/{id}/members/{userId}` — soft delete em `organization_members`; ADMIN_ORG criador não pode ser removido
- Nova tabela Flyway `invitations` (V006) com status `PENDING | USED | EXPIRED` e `expires_at`
- Frontend: página `/invitations/:token/accept` para usuário autenticado aceitar o convite

**Módulo:** `organization` (backend) + nova feature `invitation` (frontend)  
**RF:** RF-06 | **Impacto:** backend + frontend

## Non-goals

- Alterar papel de membro existente (deferido para RF posterior)
- Dashboard de listagem/gestão de membros (deferido)
- Convite para usuários já cadastrados sem envio de e-mail
- Reenvio automático de convite expirado
