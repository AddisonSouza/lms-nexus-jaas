import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle, Loader2, Mail, XCircle } from 'lucide-react'
import { confirmEmail } from '../api/auth-api'
import ResendConfirmationForm from './ResendConfirmationForm'
import { Card } from '@components/ui/card'

function StatusCard({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <Card elevation="md" className="w-full max-w-md items-center p-8 text-center">
        {children}
      </Card>
    </div>
  )
}

function StaticPendingPage() {
  return (
    <StatusCard>
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
        <Mail className="h-6 w-6" />
      </div>
      <h2 className="font-heading text-2xl">Confirme seu e-mail</h2>
      <p className="text-sm text-muted-foreground">
        Enviamos um link de confirmação para o seu e-mail. Verifique sua caixa de entrada e clique
        no link para ativar sua conta.
      </p>
      <p className="text-xs text-muted-foreground">O link expira em 24 horas.</p>
    </StatusCard>
  )
}

function ConfirmEmailCallbackPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const { isLoading, isSuccess, isError, error } = useQuery({
    queryKey: ['confirm-email', token],
    queryFn: async () => {
      await confirmEmail(token!)
      return true
    },
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
      <StatusCard>
        <Loader2 className="h-10 w-10 animate-spin text-accent" />
        <p className="text-sm text-muted-foreground">Confirmando seu e-mail...</p>
      </StatusCard>
    )
  }

  if (isSuccess) {
    return (
      <StatusCard>
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-2-100 text-accent-2-800">
          <CheckCircle className="h-6 w-6" />
        </div>
        <h2 className="font-heading text-2xl">E-mail confirmado!</h2>
        <p className="text-sm text-muted-foreground">
          Sua conta está ativa. Redirecionando para o login...
        </p>
      </StatusCard>
    )
  }

  if (isError) {
    const status = (error as { response?: { status?: number } })?.response?.status
    const isAlreadyConfirmed = status === 409

    return (
      <StatusCard>
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
          <XCircle className="h-6 w-6" />
        </div>
        <h2 className="font-heading text-2xl">
          {isAlreadyConfirmed ? 'E-mail já confirmado' : 'Link inválido ou expirado'}
        </h2>
        <p className="text-sm text-muted-foreground">
          {isAlreadyConfirmed
            ? 'Sua conta já está ativa. Acesse o login.'
            : 'O link de confirmação é inválido ou expirou.'}
        </p>
        {!isAlreadyConfirmed && <ResendConfirmationForm />}
      </StatusCard>
    )
  }

  return null
}

export default ConfirmEmailCallbackPage
