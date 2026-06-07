import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { loginSchema, type LoginFormData } from '../schemas/loginSchema'
import { useLogin } from '../hooks/useLogin'

function LoginForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  const { mutate: login, isPending, error } = useLogin()

  const onSubmit = (data: LoginFormData) => login(data)

  const errorMessage =
    (error as { response?: { status?: number } })?.response?.status === 401
      ? 'E-mail ou senha inválidos.'
      : error
        ? 'Erro ao realizar login. Tente novamente.'
        : null

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="w-full max-w-sm space-y-4 rounded-lg border p-6 shadow-sm"
      >
        <h1 className="text-2xl font-semibold">Entrar</h1>

        {errorMessage && (
          <p className="text-sm text-destructive">{errorMessage}</p>
        )}

        <div className="space-y-1">
          <label className="text-sm font-medium">E-mail</label>
          <input
            {...register('email')}
            type="email"
            autoComplete="email"
            className="w-full rounded border px-3 py-2 text-sm"
          />
          {errors.email && (
            <p className="text-xs text-destructive">{errors.email.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium">Senha</label>
          <input
            {...register('password')}
            type="password"
            autoComplete="current-password"
            className="w-full rounded border px-3 py-2 text-sm"
          />
          {errors.password && (
            <p className="text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isPending}
          className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
        >
          {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
          Entrar
        </button>

        <p className="text-center text-sm text-muted-foreground">
          <Link to="/forgot-password" className="underline underline-offset-4 hover:text-primary">
            Esqueci minha senha
          </Link>
        </p>
      </form>
    </div>
  )
}

export default LoginForm
