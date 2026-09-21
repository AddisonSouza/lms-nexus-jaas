import { z } from 'zod'
import api from '@lib/axios'

/**
 * Membros da organização, vistos pela disciplina — mesmo motivo do
 * `org-classroom-api`: `curriculum` mantém o próprio client, com o mínimo que a
 * seção precisa. `id` é o **id do vínculo** em `organization_members`, que é o
 * `memberId` esperado por `POST/DELETE /subjects/{id}/teachers` — não o `userId`.
 */
const orgMemberSchema = z.object({
  id: z.string(),
  userId: z.string(),
  name: z.string().nullable().default(null),
  email: z.string().nullable().default(null),
  role: z.enum(['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO']),
})

export type OrgMember = z.infer<typeof orgMemberSchema>

export async function listOrgMembers(organizationId: string): Promise<OrgMember[]> {
  const res = await api.get(`/organizations/${organizationId}/members`)
  return z.array(orgMemberSchema).parse(res.data)
}
