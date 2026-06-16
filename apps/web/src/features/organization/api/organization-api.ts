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
