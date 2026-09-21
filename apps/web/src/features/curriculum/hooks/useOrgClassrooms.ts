import { useQuery } from '@tanstack/react-query'
import { listOrgClassrooms } from '../api/org-classroom-api'
import { orgDirectoryKeys } from '../api/query-keys'

/** Turmas da organização, para escolher qual vincular à disciplina. */
export function useOrgClassrooms(enabled = true) {
  return useQuery({
    queryKey: orgDirectoryKeys.classrooms(),
    queryFn: listOrgClassrooms,
    enabled,
  })
}
