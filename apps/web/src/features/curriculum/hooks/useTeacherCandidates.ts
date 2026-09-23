import { useQuery } from '@tanstack/react-query'
import { listOrgMembers, type OrgMember } from '../api/org-member-api'
import { orgDirectoryKeys } from '../api/query-keys'
import { useAuthStore } from '@store/authStore'
import { canTeach } from '@lib/roles'

/**
 * Membros que a API aceita como professor da disciplina. O back recusa ALUNO
 * com `MEMBER_NOT_A_PROFESSOR`; filtrar aqui evita oferecer a escolha que vai
 * falhar. A organização vem do token (nunca do request), como no resto do app.
 */
export function useTeacherCandidates(enabled = true) {
  const organizationId = useAuthStore((s) => s.organizationId)

  return useQuery({
    queryKey: orgDirectoryKeys.members(organizationId ?? ''),
    queryFn: () => listOrgMembers(organizationId as string),
    select: (page): OrgMember[] => page.content.filter((m) => canTeach(m.role)),
    enabled: enabled && !!organizationId,
  })
}
