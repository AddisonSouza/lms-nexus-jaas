import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { loginSchema, type LoginFormData } from '../schemas/loginSchema'

interface Props {
  onSubmit: (data: LoginFormData) => void
  isPending: boolean
}

function LoginForm({ onSubmit, isPending }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  return (
    <form onSubmit={handleSubmit((data) => onSubmit(data))} className="space-y-4">
      <div className="space-y-1">
        <label htmlFor="email" className="text-sm font-medium">E-mail</label>
        <input
          {...register('email')}
          id="email"
          type="email"
          autoComplete="email"
          className="w-full rounded border px-3 py-2 text-sm"
        />
        {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
      </div>

      <div className="space-y-1">
        <label htmlFor="password" className="text-sm font-medium">Senha</label>
        <input
          {...register('password')}
          id="password"
          type="password"
          autoComplete="current-password"
          className="w-full rounded border px-3 py-2 text-sm"
        />
        {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
      </div>

      <button
        type="submit"
        disabled={isPending}
        className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
      >
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Entrar
      </button>
    </form>
  )
}

export default LoginForm
