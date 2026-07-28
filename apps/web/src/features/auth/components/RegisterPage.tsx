import { Link } from 'react-router-dom'
import { useRegister, getRegisterError } from '../hooks/useRegister'
import RegisterForm from './RegisterForm'
import AuthLayout from '@components/layout/AuthLayout'

function RegisterPage() {
  const mutation = useRegister()
  const serverError = getRegisterError(mutation.error)

  return (
    <AuthLayout>
      <div className="space-y-4">
        <div>
          <h2 className="mb-2 font-heading text-2xl">Criar conta</h2>
          <p className="text-sm text-muted-foreground">
            Você poderá fundar sua própria organização em seguida.
          </p>
        </div>

        <RegisterForm
          onSubmit={(data) => mutation.mutate(data)}
          isPending={mutation.isPending}
          serverError={serverError}
        />

        <p className="text-center text-[13px] text-muted-foreground">
          Já tem conta?{' '}
          <Link to="/login" className="text-accent hover:underline">
            Entrar
          </Link>
        </p>
      </div>
    </AuthLayout>
  )
}

export default RegisterPage
