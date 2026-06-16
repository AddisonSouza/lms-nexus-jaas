import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle, Loader2, Mail, XCircle } from 'lucide-react'
import { confirmEmail } from '../api/auth-api'
import ResendConfirmationForm from './ResendConfirmationForm'

function StaticPendingPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="w-full max-w-md rounded-lg border p-8 shadow-sm text-center">
        <Mail className="mx-auto mb-4 h-12 w-12 text-primary" />
        <h1 className="text-2xl font-semibold mb-2">Confirme seu e-mail</h1>
        <p className="text-sm text-muted-foreground">
          Enviamos um link de confirmação para o seu e-mail. Verifique sua caixa de entrada e clique
          no link para ativar sua conta.
        </p>
        <p className="mt-4 text-xs text-muted-foreground">O link expira em 24 horas.</p>
      </div>
    </div>
  )
}

function ConfirmEmailCallbackPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const { isLoading, isSuccess, isError, error } = useQuery({
    queryKey: ['confirm-email', token],
    queryFn: () => confirmEmail(token!),
    enabled: !!token,
    retry: false,
    staleTime: Infinity,
  })

  useEffect(() => {
    if (isSuccess) {
      const timer = setTimeout(() => navigate('/login?confirmed=true'), 2000)
      return () => clearTimeout(timer)
    }
  }, [isSuccess, navigate])

  if (!token) {
    return <StaticPendingPage />
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="w-full max-w-md rounded-lg border p-8 shadow-sm text-center">
          <Loader2 className="mx-auto mb-4 h-12 w-12 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">Confirmando seu e-mail...</p>
        </div>
      </div>
    )
  }

  if (isSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="w-full max-w-md rounded-lg border p-8 shadow-sm text-center">
          <CheckCircle className="mx-auto mb-4 h-12 w-12 text-primary" />
          <h1 className="text-2xl font-semibold mb-2">E-mail confirmado!</h1>
          <p className="text-sm text-muted-foreground">
            Sua conta está ativa. Redirecionando para o login...
          </p>
        </div>
      </div>
    )
  }

  if (isError) {
    const status = (error as { response?: { status?: number } })?.response?.status
    const isAlreadyConfirmed = status === 409

    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="w-full max-w-md rounded-lg border p-8 shadow-sm text-center">
          <XCircle className="mx-auto mb-4 h-12 w-12 text-destructive" />
          <h1 className="text-2xl font-semibold mb-2">
            {isAlreadyConfirmed ? 'E-mail já confirmado' : 'Link inválido ou expirado'}
          </h1>
          <p className="text-sm text-muted-foreground">
            {isAlreadyConfirmed
              ? 'Sua conta já está ativa. Acesse o login.'
              : 'O link de confirmação é inválido ou expirou.'}
          </p>
          {!isAlreadyConfirmed && <ResendConfirmationForm />}
        </div>
      </div>
    )
  }

  return null
}

export default ConfirmEmailCallbackPage
