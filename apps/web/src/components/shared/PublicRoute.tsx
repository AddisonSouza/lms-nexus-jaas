import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import FullScreenLoader from './FullScreenLoader'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

function PublicRoute({ children }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const isBootstrapping = useAuthStore((s) => s.isBootstrapping)
  if (isBootstrapping) return <FullScreenLoader />
  return isAuthenticated ? <Navigate to="/" replace /> : <>{children}</>
}

export default PublicRoute
