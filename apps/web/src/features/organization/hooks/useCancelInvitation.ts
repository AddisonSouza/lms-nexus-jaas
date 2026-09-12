import { useMutation, useQueryClient } from '@tanstack/react-query'
import { cancelInvitation } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useCancelInvitation(organizationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (invitationId: string) => cancelInvitation(organizationId, invitationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: organizationKeys.invitations(organizationId) })
    },
  })
}
