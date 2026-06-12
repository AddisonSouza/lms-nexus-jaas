import { useParams } from 'react-router-dom'

function OrganizationDashboardPage() {
  const { id } = useParams<{ id: string }>()
  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <p className="text-muted-foreground">Dashboard da organização {id} — em breve (RF-17)</p>
    </div>
  )
}

export default OrganizationDashboardPage
