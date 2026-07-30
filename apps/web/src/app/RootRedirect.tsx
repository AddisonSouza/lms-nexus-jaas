import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'

function RootRedirect() {
  const organizationId = useAuthStore((s) => s.organizationId)
  return <Navigate to={organizationId ? '/classrooms' : '/welcome'} replace />
}

export default RootRedirect
