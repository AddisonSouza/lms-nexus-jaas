import { Link } from 'react-router-dom'
import { Building2, Mail, TicketCheck } from 'lucide-react'
import { Card, CardBody, CardKicker, CardTitle } from '@components/ui/card'
import { Button } from '@components/ui/button'
import InviteLinkForm from './InviteLinkForm'

function WelcomePage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-6">
      <div className="w-full max-w-3xl space-y-6">
        <div className="space-y-2 text-center">
          <h1 className="font-heading text-2xl">Bem-vindo ao LMS Nexus</h1>
          <p className="text-sm text-muted-foreground">
            Você ainda não faz parte de nenhuma organização. Escolha por onde começar.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Card elevation="md" className="p-6">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accent-100 text-accent-800">
              <Building2 className="h-5 w-5" />
            </div>
            <CardKicker>Criar</CardKicker>
            <CardTitle>Criar uma organização</CardTitle>
            <CardBody>
              Monte a sua organização e convide gestores, professores e alunos.
            </CardBody>
            <Button render={<Link to="/organizations/new" />} className="w-full">
              Criar organização
            </Button>
          </Card>

          <Card elevation="md" className="p-6">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accent-2-100 text-accent-2-800">
              <TicketCheck className="h-5 w-5" />
            </div>
            <CardKicker>Entrar</CardKicker>
            <CardTitle>Entrar em uma organização</CardTitle>
            <CardBody>
              Já recebeu um convite? Use o link enviado para entrar na organização.
            </CardBody>
            <InviteLinkForm />
            <div className="flex items-start gap-2 rounded-[var(--radius-md)] bg-muted px-3 py-2 text-[13px] text-muted-foreground">
              <Mail className="mt-0.5 h-4 w-4 shrink-0" />
              <span>
                O administrador ou gestor de uma organização já existente pode convidar você
                por e-mail. O link de acesso chega na mensagem.
              </span>
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default WelcomePage
