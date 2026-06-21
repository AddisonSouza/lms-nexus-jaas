import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { createOrganization, type CreateOrganizationData } from '../api/organization-api'
import { useAuthStore } from '@store/authStore'
import api from '@lib/axios'

export function useCreateOrganization() {
  const navigate = useNavigate()
  const setToken = useAuthStore((s) => s.setToken)

  return useMutation({
    mutationFn: (data: CreateOrganizationData) => createOrganization(data),
    onSuccess: async (org) => {
      const response = await api.post<{ accessToken: string }>(
        '/auth/switch-organization',
        { organizationId: org.id },
        { withCredentials: true },
      )
      setToken(response.data.accessToken)
      navigate(`/organizations/${org.id}`)
    },
  })
}
