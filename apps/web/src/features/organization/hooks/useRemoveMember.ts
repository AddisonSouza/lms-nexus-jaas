import { useMutation, useQueryClient } from '@tanstack/react-query'
import { removeMember } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useRemoveMember(organizationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (userId: string) => removeMember(organizationId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: organizationKeys.members(organizationId) })
    },
  })
}
