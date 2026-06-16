import { useRegister, getRegisterError } from '../hooks/useRegister'
import RegisterForm from './RegisterForm'

function RegisterPage() {
  const mutation = useRegister()
  const serverError = getRegisterError(mutation.error)

  return (
    <RegisterForm
      onSubmit={(data) => mutation.mutate(data)}
      isPending={mutation.isPending}
      serverError={serverError}
    />
  )
}

export default RegisterPage
