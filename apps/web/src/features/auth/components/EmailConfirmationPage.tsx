import { Mail } from 'lucide-react'

function EmailConfirmationPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted">
      <div className="w-full max-w-md bg-background rounded-lg shadow p-8 text-center">
        <div className="flex justify-center mb-4">
          <Mail className="h-12 w-12 text-primary" />
        </div>
        <h1 className="text-2xl font-semibold text-foreground mb-2">Confirme seu e-mail</h1>
        <p className="text-muted-foreground text-sm">
          Enviamos um link de confirmação para o seu e-mail. Verifique sua caixa de entrada e clique
          no link para ativar sua conta.
        </p>
        <p className="mt-4 text-xs text-muted-foreground">O link expira em 24 horas.</p>
      </div>
    </div>
  )
}

export default EmailConfirmationPage
