import { z } from 'zod'
import api from '@lib/axios'

/**
 * Turmas da organização, vistas pela disciplina. `curriculum` não pode importar
 * o client de `classroom` (features não se importam), então mantém aqui o
 * mínimo que a seção "Turmas e Professores" desenha: nome e status. Campos
 * novos em `/classrooms` são ignorados em vez de derrubar o parse da lista.
 */
const orgClassroomSchema = z.object({
  id: z.string(),
  name: z.string(),
  status: z.enum(['ACTIVE', 'ARCHIVED']).default('ACTIVE'),
})

export type OrgClassroom = z.infer<typeof orgClassroomSchema>

/** ADMIN_ORG e GESTOR recebem todas as turmas da org, inclusive as arquivadas. */
export async function listOrgClassrooms(): Promise<OrgClassroom[]> {
  const res = await api.get('/classrooms')
  return z.array(orgClassroomSchema).parse(res.data)
}
