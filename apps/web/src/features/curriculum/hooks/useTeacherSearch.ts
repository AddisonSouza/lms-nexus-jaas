import { useQuery } from '@tanstack/react-query'
import { listOrgMembers, type OrgMember } from '../api/org-member-api'
import { orgDirectoryKeys } from '../api/query-keys'
import { useAuthStore } from '@store/authStore'
import { useDebouncedValue } from '@hooks/useDebouncedValue'
import { canTeach } from '@lib/roles'

const PAGE_SIZE = 20

/**
 * Candidatos a professor, buscados por nome ou e-mail. Quem filtra pelo termo é
 * a API; o `canTeach` continua no cliente porque o endpoint não filtra por
 * papel — a API recusaria um ALUNO com `MEMBER_NOT_A_PROFESSOR`, e oferecer a
 * escolha que vai falhar é pior do que escondê-la.
 *
 * Separado do `useTeacherCandidates`, que a página usa para ter a lista inteira.
 */
export function useTeacherSearch(term: string, enabled = true) {
  const organizationId = useAuthStore((s) => s.organizationId)
  const search = useDebouncedValue(term.trim(), 300)

  return useQuery({
    queryKey: orgDirectoryKeys.search(organizationId ?? '', search),
    queryFn: () => listOrgMembers(organizationId as string, { search, size: PAGE_SIZE }),
    select: (page): OrgMember[] => page.content.filter((m) => canTeach(m.role)),
    enabled: enabled && !!organizationId,
    // Segura a lista anterior enquanto a busca nova viaja, para não piscar.
    placeholderData: (previous) => previous,
  })
}
