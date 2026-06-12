import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { createOrganization, type CreateOrganizationData } from '../api/organization-api'
import { refreshTokens } from '@features/auth/api/auth-api'
import { useAuthStore } from '@features/auth/store/authStore'

export function useCreateOrganization() {
  const navigate = useNavigate()
  const setOrganization = useAuthStore((s) => s.setOrganization)

  return useMutation({
    mutationFn: (data: CreateOrganizationData) => createOrganization(data),
    onSuccess: async (org) => {
      const refreshed = await refreshTokens(org.id)
      setOrganization(refreshed.accessToken, org.id)
      navigate(`/organizations/${org.id}`)
    },
  })
}
