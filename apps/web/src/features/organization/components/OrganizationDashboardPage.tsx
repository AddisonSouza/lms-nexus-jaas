import { Link, useParams } from 'react-router-dom'
import { BookOpen } from 'lucide-react'
import { useAuthStore } from '@features/auth/store/authStore'
import AdminDashboard from '@features/dashboard/components/AdminDashboard'
import GestorDashboard from '@features/dashboard/components/GestorDashboard'

function OrganizationDashboardPage() {
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

  return (
    <div className="container mx-auto max-w-4xl p-6 space-y-6">
      <h1 className="text-2xl font-semibold">Organização</h1>
      <p className="text-muted-foreground text-sm">ID: {id}</p>

      <nav className="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <Link
          to="/classrooms"
          className="flex items-center gap-3 rounded-lg border p-4 hover:bg-muted transition-colors"
        >
          <BookOpen className="h-5 w-5 text-primary" />
          <span className="font-medium">Turmas</span>
        </Link>
      </nav>
    </div>
  )
}

export default OrganizationDashboardPage
