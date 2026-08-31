import { useQuery } from '@tanstack/react-query'
import { listPendingInvitations } from '../api/invitation-api'

export const pendingInvitationsKey = ['invitations', 'pending'] as const

export function usePendingInvitations(enabled = true) {
  return useQuery({
    queryKey: pendingInvitationsKey,
    queryFn: listPendingInvitations,
    enabled,
    retry: false,
  })
}
