import { useQuery } from '@tanstack/react-query'
import { listOrganizations } from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

export function useOrganizations() {
  return useQuery({
    queryKey: organizationKeys.lists(),
    queryFn: listOrganizations,
  })
}
