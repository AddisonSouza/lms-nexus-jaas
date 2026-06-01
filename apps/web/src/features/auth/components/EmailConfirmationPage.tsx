import { Mail } from 'lucide-react'

function EmailConfirmationPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-md bg-white rounded-lg shadow p-8 text-center">
        <div className="flex justify-center mb-4">
          <Mail className="h-12 w-12 text-blue-600" />
        </div>
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Confirme seu e-mail</h1>
        <p className="text-gray-600 text-sm">
          Enviamos um link de confirmação para o seu e-mail. Verifique sua caixa de entrada e clique
          no link para ativar sua conta.
        </p>
        <p className="mt-4 text-xs text-gray-400">O link expira em 24 horas.</p>
      </div>
    </div>
  )
}

export default EmailConfirmationPage
