import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@features/auth/store/authStore'

function RootRedirect() {
  const organizationId = useAuthStore((s) => s.organizationId)
  return <Navigate to={organizationId ? '/classrooms' : '/organizations/new'} replace />
}

export default RootRedirect
