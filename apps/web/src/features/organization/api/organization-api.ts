import api from '@lib/axios'

export interface CreateOrganizationData {
  name: string
  description?: string
}

export interface OrganizationResponse {
  id: string
  name: string
  description: string | null
  ownerId: string
  createdAt: string
}

export async function createOrganization(data: CreateOrganizationData): Promise<OrganizationResponse> {
  const response = await api.post<OrganizationResponse>('/organizations', data)
  return response.data
}
