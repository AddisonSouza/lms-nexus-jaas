import api from '@lib/axios'

export interface InvitationInfo {
  organizationId: string
  organizationName: string
  email: string
  role: 'ADMIN_ORG' | 'MEMBER' | 'PROFESSOR' | 'ALUNO'
  expiresAt: string
}

export async function getInvitationInfo(token: string): Promise<InvitationInfo> {
  const response = await api.get<InvitationInfo>(`/invitations/${token}`)
  return response.data
}

export async function acceptInvitation(token: string): Promise<void> {
  await api.post(`/invitations/${token}/accept`)
}
