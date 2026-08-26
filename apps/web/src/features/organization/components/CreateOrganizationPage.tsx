import { Link } from 'react-router-dom'
import { Building2 } from 'lucide-react'
import { Card, CardKicker } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { useAuthStore } from '@store/authStore'
import CreateOrganizationForm from './CreateOrganizationForm'

function CreateOrganizationPage() {
  const organizationId = useAuthStore((s) => s.organizationId)
  // /welcome tells the user they belong to no organization; whoever already has
  // one got here from inside the app, so send them back to their dashboard.
  const backTo = organizationId ? `/organizations/${organizationId}` : '/welcome'

  return (
    <div className="w-full max-w-md space-y-6">
      <div className="space-y-2 text-center">
        <h1 className="font-heading text-2xl">Criar organização</h1>
        <p className="text-sm text-muted-foreground">
          A organização é o espaço onde ficam suas turmas, disciplinas e membros.
          Você será o administrador dela.
        </p>
      </div>

      <Card elevation="md" className="p-6">
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accent-100 text-accent-800">
          <Building2 className="h-5 w-5" />
        </div>
        <CardKicker>Criar</CardKicker>
        <CreateOrganizationForm />
        <Button
          render={<Link to={backTo} />}
          nativeButton={false}
          variant="ghost"
          className="w-full"
        >
          Voltar
        </Button>
      </Card>
    </div>
  )
}

export default CreateOrganizationPage
