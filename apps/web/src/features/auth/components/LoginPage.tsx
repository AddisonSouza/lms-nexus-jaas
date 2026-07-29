import { CheckCircle } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { useLogin } from '../hooks/useLogin'
import LoginForm from './LoginForm'
import AuthLayout from '@components/layout/AuthLayout'

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
    <AuthLayout>
      <div className="space-y-4">
        <div>
          <h2 className="mb-2 font-heading text-2xl">Entrar</h2>
          <p className="text-sm text-muted-foreground">Use o e-mail cadastrado na sua organização.</p>
        </div>

        {justConfirmed && (
          <div className="flex items-center gap-2 rounded-[var(--radius-md)] bg-accent-2-100 px-3 py-2 text-sm text-accent-2-800">
            <CheckCircle className="h-4 w-4 shrink-0" />
            E-mail confirmado com sucesso! Faça login para continuar.
          </div>
        )}

        {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}

        <LoginForm onSubmit={login} isPending={isPending} />

        <hr className="border-border" />

        <p className="text-center text-sm text-muted-foreground">
          <Link to="/forgot-password" className="text-accent hover:underline">
            Esqueci minha senha
          </Link>
        </p>
        <p className="text-center text-[13px] text-muted-foreground">
          Ainda não tem conta?{' '}
          <Link to="/register" className="text-accent hover:underline">
            Criar conta
          </Link>{' '}
          — você poderá fundar sua própria organização.
        </p>
      </div>
    </AuthLayout>
  )
}

export default LoginPage
