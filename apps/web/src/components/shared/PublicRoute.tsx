import { Navigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import FullScreenLoader from './FullScreenLoader'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

function PublicRoute({ children }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const isBootstrapping = useAuthStore((s) => s.isBootstrapping)
  const [searchParams] = useSearchParams()

  if (isBootstrapping) return <FullScreenLoader />
  if (!isAuthenticated) return <>{children}</>

  // Quem entra (ou já chega logado) por um link de convite volta para o aceite.
  // Mandar para / passava por cima do useLogin, e em / o RootRedirect só leva ao
  // aceite se o convite ainda estiver pendente — cancelado ou reenviado caía em
  // /welcome sem explicação (#198).
  const inviteToken = searchParams.get('invite')
  return (
    <Navigate to={inviteToken ? `/invitations/${encodeURIComponent(inviteToken)}/accept` : '/'} replace />
  )
}

export default PublicRoute
