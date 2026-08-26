import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { createOrganization, switchOrganization, type CreateOrganizationData } from '../api/organization-api'
import { useAuthStore } from '@store/authStore'

export function useCreateOrganization() {
  const navigate = useNavigate()
  const setToken = useAuthStore((s) => s.setToken)

  return useMutation({
    mutationFn: (data: CreateOrganizationData) => createOrganization(data),
    onSuccess: async (org) => {
      setToken(await switchOrganization(org.id))
      navigate(`/organizations/${org.id}`)
    },
  })
}
