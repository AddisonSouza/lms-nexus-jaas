import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@features/auth/store/authStore'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
  roles?: string[]
}

function ProtectedRoute({ children, roles }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const role = useAuthStore((s) => s.role)

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (roles && role && !roles.includes(role)) return <Navigate to="/" replace />

  return <>{children}</>
}

export default ProtectedRoute
