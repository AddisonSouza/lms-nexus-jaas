import { useMutation, useQueryClient } from '@tanstack/react-query'
import { changeMemberRole, type AssignableRole } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useChangeMemberRole(organizationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, role }: { userId: string; role: AssignableRole }) =>
      changeMemberRole(organizationId, userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: organizationKeys.members(organizationId) })
    },
  })
}
