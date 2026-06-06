import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@features/auth/store/authStore'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

function PublicRoute({ children }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return isAuthenticated ? <Navigate to="/" replace /> : <>{children}</>
}

export default PublicRoute
