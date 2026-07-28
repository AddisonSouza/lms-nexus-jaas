import { Link, useParams } from 'react-router-dom'
import { BookOpen } from 'lucide-react'
import { Card } from '@components/ui/card'

function OrganizationDashboardPage() {
  const { id } = useParams<{ id: string }>()

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div>
        <h2 className="mb-1">Organização</h2>
        <p className="text-sm text-muted-foreground">ID: {id}</p>
      </div>

      <nav className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <Link to="/classrooms">
          <Card elevation="sm" className="flex-row items-center gap-3 transition-colors hover:bg-muted">
            <BookOpen className="h-5 w-5 text-accent" />
            <span className="font-medium">Turmas</span>
          </Card>
        </Link>
      </nav>
    </div>
  )
}

export default OrganizationDashboardPage
