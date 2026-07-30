import { Link } from 'react-router-dom'
import { Building2 } from 'lucide-react'
import { Card } from '@components/ui/card'
import { Button } from '@components/ui/button'

/** Estado vazio das áreas internas para quem ainda não pertence a uma organização. */
function NoOrganizationState() {
  return (
    <Card elevation="md" className="mx-auto max-w-md items-center p-8 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
        <Building2 className="h-6 w-6" />
      </div>
      <h2 className="font-heading text-xl">Você não faz parte de nenhuma organização</h2>
      <p className="text-sm text-muted-foreground">
        Esta área só fica disponível dentro de uma organização. Crie a sua ou entre em uma
        existente pelo convite que o administrador ou gestor enviar por e-mail.
      </p>
      <div className="flex flex-wrap justify-center gap-2">
        <Button render={<Link to="/organizations/new" />}>Criar organização</Button>
        <Button variant="secondary" render={<Link to="/welcome" />}>
          Entrar por convite
        </Button>
      </div>
    </Card>
  )
}

export default NoOrganizationState
