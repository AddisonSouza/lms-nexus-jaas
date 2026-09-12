import { useQuery } from '@tanstack/react-query'
import { listInvitations } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useOrganizationInvitations(organizationId: string) {
  return useQuery({
    queryKey: organizationKeys.invitations(organizationId),
    queryFn: () => listInvitations(organizationId),
    enabled: !!organizationId,
  })
}
