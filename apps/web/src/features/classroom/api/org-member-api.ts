import { z } from 'zod'
import api from '@lib/axios'
import { pageSchema, type Page, type PageParams } from '@lib/pagination'

/**
 * Membros da organização, vistos pela turma. `classroom` mantém o próprio
 * client pelo mesmo motivo que `curriculum` mantém o dele: uma feature não
 * importa da outra. O que interessa aqui é o `userId` — é ele que
 * `POST /classrooms/{id}/members` espera, não o id do vínculo.
 */
const orgMemberSchema = z.object({
  id: z.string(),
  userId: z.string(),
  name: z.string().nullable().default(null),
  email: z.string().nullable().default(null),
  role: z.enum(['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO']),
})

export type OrgMember = z.infer<typeof orgMemberSchema>

export async function searchOrgMembers(
  organizationId: string,
  params: PageParams = {}
): Promise<Page<OrgMember>> {
  const res = await api.get(`/organizations/${organizationId}/members`, { params })
  return pageSchema(orgMemberSchema).parse(res.data)
}
