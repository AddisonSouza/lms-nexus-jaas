import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { registerSchema, type RegisterFormData } from '../schemas/registerSchema'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface Props {
  onSubmit: (data: RegisterFormData) => void
  isPending: boolean
  serverError: string | null
}

function RegisterForm({ onSubmit, isPending, serverError }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  })

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-3">
      <div className="space-y-1">
        <label htmlFor="fullName" className="text-xs text-muted-foreground">Nome completo</label>
        <Input id="fullName" type="text" autoComplete="name" {...register('fullName')} />
        {errors.fullName && <p className="text-xs text-destructive">{errors.fullName.message}</p>}
      </div>

      <div className="space-y-1">
        <label htmlFor="email" className="text-xs text-muted-foreground">E-mail</label>
        <Input id="email" type="email" autoComplete="email" {...register('email')} />
        {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
      </div>

      <div className="space-y-1">
        <label htmlFor="password" className="text-xs text-muted-foreground">Senha</label>
        <Input id="password" type="password" autoComplete="new-password" {...register('password')} />
        {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
      </div>

      {serverError && (
        <p className="rounded-[var(--radius-md)] bg-accent-100 px-3 py-2 text-sm text-accent-800">
          {serverError}
        </p>
      )}

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        {isPending ? 'Criando conta...' : 'Criar conta'}
      </Button>
    </form>
  )
}

export default RegisterForm
