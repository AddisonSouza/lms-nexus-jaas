import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { createOrganization, type CreateOrganizationData } from '../api/organization-api'
import { useAuthStore } from '@features/auth/store/authStore'
import api from '@lib/axios'

export function useCreateOrganization() {
  const navigate = useNavigate()
  const setOrganization = useAuthStore((s) => s.setOrganization)

  return useMutation({
    mutationFn: (data: CreateOrganizationData) => createOrganization(data),
    onSuccess: async (org) => {
      const response = await api.post<{ accessToken: string }>(
        '/auth/refresh',
        { organizationId: org.id },
        { withCredentials: true },
      )
      setOrganization(response.data.accessToken, org.id)
      navigate(`/organizations/${org.id}`)
    },
  })
}
