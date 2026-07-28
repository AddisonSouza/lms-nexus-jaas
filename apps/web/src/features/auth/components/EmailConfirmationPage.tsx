import { Mail } from 'lucide-react'
import { Card } from '@components/ui/card'

function EmailConfirmationPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <Card elevation="md" className="w-full max-w-md items-center p-8 text-center">
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
          <Mail className="h-6 w-6" />
        </div>
        <h2 className="font-heading text-2xl">Confirme seu e-mail</h2>
        <p className="text-sm text-muted-foreground">
          Enviamos um link de confirmação para o seu e-mail. Verifique sua caixa de entrada e clique
          no link para ativar sua conta.
        </p>
        <p className="text-xs text-muted-foreground">O link expira em 24 horas.</p>
      </Card>
    </div>
  )
}

export default EmailConfirmationPage
