import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { forgotPasswordSchema, type ForgotPasswordFormData } from '../schemas/forgotPasswordSchema'
import { useForgotPassword } from '../hooks/useForgotPassword'
import AuthLayout from '@components/layout/AuthLayout'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

function ForgotPasswordPage() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
  })

  const { mutate: requestReset, isPending, isSuccess } = useForgotPassword()

  const onSubmit = (data: ForgotPasswordFormData) => requestReset(data.email)

  if (isSuccess) {
    return (
      <AuthLayout>
        <div className="space-y-2 text-center">
          <h2 className="font-heading text-2xl">E-mail enviado</h2>
          <p className="text-sm text-muted-foreground">
            Se houver uma conta com esse e-mail, você receberá um link para redefinir sua senha em breve.
          </p>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <h2 className="mb-2 font-heading text-2xl">Esqueci minha senha</h2>
          <p className="text-sm text-muted-foreground">
            Informe seu e-mail e enviaremos um link para redefinir sua senha.
          </p>
        </div>

        <div className="space-y-1">
          <label htmlFor="email" className="text-xs text-muted-foreground">E-mail</label>
          <Input {...register('email')} id="email" type="email" autoComplete="email" />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>

        <Button type="submit" disabled={isPending} className="w-full">
          {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
          Enviar link
        </Button>
      </form>
    </AuthLayout>
  )
}

export default ForgotPasswordPage
