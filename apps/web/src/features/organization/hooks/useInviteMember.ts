import { useMutation, useQueryClient } from '@tanstack/react-query'
import { inviteMember, type InviteMemberData } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useInviteMember(organizationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: InviteMemberData) => inviteMember(organizationId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: organizationKeys.members(organizationId) })
    },
  })
}
