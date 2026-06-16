import { z } from 'zod'
import api from '@lib/axios'

const invitationInfoSchema = z.object({
  organizationId: z.string(),
  organizationName: z.string(),
  email: z.string(),
  role: z.enum(['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO']),
  expiresAt: z.string(),
})

export type InvitationInfo = z.infer<typeof invitationInfoSchema>

export async function getInvitationInfo(token: string): Promise<InvitationInfo> {
  const response = await api.get(`/invitations/${token}`)
  return invitationInfoSchema.parse(response.data)
}

export async function acceptInvitation(token: string): Promise<void> {
  await api.post(`/invitations/${token}/accept`)
}
