import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import { usePendingInvitations } from '@features/invitation/hooks/usePendingInvitations'
import FullScreenLoader from '@components/shared/FullScreenLoader'

function RootRedirect() {
  const organizationId = useAuthStore((s) => s.organizationId)

  // Só quem chega sem organização é o recém-convidado que ainda não aceitou;
  // quem já tem organização segue para o app sem ser desviado por um convite
  // pendente que talvez não queira.
  const { data: pending, isLoading } = usePendingInvitations(!organizationId)

  if (organizationId) return <Navigate to="/classrooms" replace />

  if (isLoading) return <FullScreenLoader />

  const invitation = pending?.[0]
  if (invitation) {
    return <Navigate to={`/invitations/${invitation.token}/accept`} replace />
  }

  return <Navigate to="/welcome" replace />
}

export default RootRedirect
