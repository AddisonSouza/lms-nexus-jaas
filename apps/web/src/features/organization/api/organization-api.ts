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

const switchOrganizationSchema = z.object({
  accessToken: z.string(),
})

export async function switchOrganization(organizationId: string): Promise<string> {
  const response = await api.post(
    '/auth/switch-organization',
    { organizationId },
    { withCredentials: true },
  )
  return switchOrganizationSchema.parse(response.data).accessToken
}
