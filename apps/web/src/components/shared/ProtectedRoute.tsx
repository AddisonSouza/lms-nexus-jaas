import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@features/auth/store/authStore'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

function ProtectedRoute({ children }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

export default ProtectedRoute
