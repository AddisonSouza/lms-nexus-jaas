import { CheckCircle } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { useLogin } from '../hooks/useLogin'
import LoginForm from './LoginForm'

function LoginPage() {
  const [searchParams] = useSearchParams()
  const justConfirmed = searchParams.get('confirmed') === 'true'
  const { mutate: login, isPending, error } = useLogin()

  const errorMessage =
    (error as { response?: { status?: number } })?.response?.status === 401
      ? 'E-mail ou senha inválidos.'
      : error
        ? 'Erro ao realizar login. Tente novamente.'
        : null

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <div className="w-full max-w-sm space-y-4 rounded-lg border p-6 shadow-sm">
        <h1 className="text-2xl font-semibold">Entrar</h1>

        {justConfirmed && (
          <div className="flex items-center gap-2 rounded border border-success/30 bg-success/10 px-3 py-2 text-sm text-success">
            <CheckCircle className="h-4 w-4 shrink-0" />
            E-mail confirmado com sucesso! Faça login para continuar.
          </div>
        )}

        {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}

        <LoginForm onSubmit={login} isPending={isPending} />

        <p className="text-center text-sm text-muted-foreground">
          <Link to="/forgot-password" className="underline underline-offset-4 hover:text-primary">
            Esqueci minha senha
          </Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
