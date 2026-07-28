import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { loginSchema, type LoginFormData } from '../schemas/loginSchema'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

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
    <form onSubmit={handleSubmit((data) => onSubmit(data))} className="space-y-3">
      <div className="space-y-1">
        <label htmlFor="email" className="text-xs text-muted-foreground">E-mail</label>
        <Input {...register('email')} id="email" type="email" autoComplete="email" />
        {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
      </div>

      <div className="space-y-1">
        <label htmlFor="password" className="text-xs text-muted-foreground">Senha</label>
        <Input {...register('password')} id="password" type="password" autoComplete="current-password" />
        {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
      </div>

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Entrar
      </Button>
    </form>
  )
}

export default LoginForm
