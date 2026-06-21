import { useParams } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import AdminDashboard from '@features/dashboard/components/AdminDashboard'
import GestorDashboard from '@features/dashboard/components/GestorDashboard'
import StudentDashboard from '@features/dashboard/components/StudentDashboard'
import OrganizationDashboardPage from '@features/organization/components/OrganizationDashboardPage'

function OrganizationRoute() {
  const { id } = useParams<{ id: string }>()
  const role = useAuthStore((s) => s.role)

  if (role === 'ADMIN_ORG' && id) {
    return (
      <div className="container mx-auto max-w-6xl p-6 space-y-6">
        <h1 className="text-2xl font-semibold">Organização</h1>
        <AdminDashboard organizationId={id} />
      </div>
    )
  }

  if (role === 'GESTOR' && id) {
    return (
      <div className="container mx-auto max-w-6xl p-6 space-y-6">
        <h1 className="text-2xl font-semibold">Organização</h1>
        <GestorDashboard organizationId={id} />
      </div>
    )
  }

  if (role === 'ALUNO') {
    return (
      <div className="container mx-auto max-w-6xl p-6 space-y-6">
        <h1 className="text-2xl font-semibold">Meu Dashboard</h1>
        <StudentDashboard />
      </div>
    )
  }

  return <OrganizationDashboardPage />
}

export default OrganizationRoute
