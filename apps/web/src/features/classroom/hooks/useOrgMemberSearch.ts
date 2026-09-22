import { useQuery } from '@tanstack/react-query'
import { searchOrgMembers, type OrgMember } from '../api/org-member-api'
import { orgDirectoryKeys } from '../api/query-keys'
import { useAuthStore } from '@store/authStore'
import { useDebouncedValue } from '@hooks/useDebouncedValue'
import type { Page } from '@lib/pagination'

const PAGE_SIZE = 20

/**
 * Busca membros da organização por nome ou e-mail. Quem filtra é a API: o termo
 * vai no query string, já atrasado pelo debounce para não render uma requisição
 * por tecla. A organização vem do token, nunca do request.
 */
export function useOrgMemberSearch(term: string, enabled = true) {
  const organizationId = useAuthStore((s) => s.organizationId)
  const search = useDebouncedValue(term.trim(), 300)

  return useQuery<Page<OrgMember>>({
    queryKey: orgDirectoryKeys.search(organizationId ?? '', search),
    queryFn: () => searchOrgMembers(organizationId as string, { search, size: PAGE_SIZE }),
    enabled: enabled && !!organizationId,
    // Segura a lista anterior enquanto a busca nova viaja; sem isso a lista
    // pisca a cada termo que sobrevive ao debounce.
    placeholderData: (previous) => previous,
  })
}
