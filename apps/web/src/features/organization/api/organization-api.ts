import { z } from 'zod'
import api from '@lib/axios'

export interface CreateOrganizationData {
  name: string
  description?: string
}

const organizationSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string().nullable(),
  ownerId: z.string(),
  createdAt: z.string(),
})

export type OrganizationResponse = z.infer<typeof organizationSchema>

export async function createOrganization(data: CreateOrganizationData): Promise<OrganizationResponse> {
  const response = await api.post('/organizations', data)
  return organizationSchema.parse(response.data)
}

const userOrganizationSchema = z.object({
  id: z.string(),
  name: z.string(),
  role: z.enum(['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO']),
})

export type UserOrganization = z.infer<typeof userOrganizationSchema>

export async function listOrganizations(): Promise<UserOrganization[]> {
  const response = await api.get('/organizations')
  return z.array(userOrganizationSchema).parse(response.data)
}

const memberRoleSchema = z.enum(['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO'])

/** ADMIN_ORG pertence a quem criou a organização — não se convida nem se atribui. */
export const assignableRoleSchema = z.enum(['GESTOR', 'PROFESSOR', 'ALUNO'])

export type MemberRole = z.infer<typeof memberRoleSchema>
export type AssignableRole = z.infer<typeof assignableRoleSchema>

const organizationMemberSchema = z.object({
  id: z.string(),
  userId: z.string(),
  name: z.string().nullable(),
  email: z.string().nullable(),
  role: memberRoleSchema,
  joinedAt: z.string(),
  owner: z.boolean(),
})

export type OrganizationMember = z.infer<typeof organizationMemberSchema>

export async function listMembers(organizationId: string): Promise<OrganizationMember[]> {
  const response = await api.get(`/organizations/${organizationId}/members`)
  return z.array(organizationMemberSchema).parse(response.data)
}

export interface InviteMemberData {
  email: string
  role: AssignableRole
}

export async function inviteMember(organizationId: string, data: InviteMemberData): Promise<void> {
  await api.post(`/organizations/${organizationId}/invitations`, data)
}

export async function changeMemberRole(
  organizationId: string,
  userId: string,
  role: AssignableRole,
): Promise<void> {
  await api.patch(`/organizations/${organizationId}/members/${userId}`, { role })
}

export async function removeMember(organizationId: string, userId: string): Promise<void> {
  await api.delete(`/organizations/${organizationId}/members/${userId}`)
}
