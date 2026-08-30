import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { switchOrganization } from '@lib/session'
import { useAuthStore } from '@store/authStore'

export function useSwitchOrganization() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const setToken = useAuthStore((s) => s.setToken)

  return useMutation({
    mutationFn: (organizationId: string) => switchOrganization(organizationId),
    onSuccess: (accessToken) => {
      // The new token carries the role and organization of the target org.
      setToken(accessToken)
      // Every cached query belongs to the previous organization.
      queryClient.clear()
      // RootRedirect picks the landing route from the new role.
      navigate('/', { replace: true })
    },
  })
}
