import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { createOrganization, switchOrganization, type CreateOrganizationData } from '../api/organization-api'
import { useAuthStore } from '@store/authStore'

export function useCreateOrganization() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const setToken = useAuthStore((s) => s.setToken)

  return useMutation({
    mutationFn: (data: CreateOrganizationData) => createOrganization(data),
    onSuccess: async (org) => {
      // Creating an organization also switches into it, so — as in
      // useSwitchOrganization — every cached query belongs to the previous
      // context, the list of organizations included.
      setToken(await switchOrganization(org.id))
      queryClient.clear()
      navigate(`/organizations/${org.id}`)
    },
  })
}
