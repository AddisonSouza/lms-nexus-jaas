import { useQuery } from '@tanstack/react-query'
import { listMembers } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useOrganizationMembers(organizationId: string) {
  return useQuery({
    queryKey: organizationKeys.members(organizationId),
    queryFn: () => listMembers(organizationId),
    enabled: !!organizationId,
  })
}
