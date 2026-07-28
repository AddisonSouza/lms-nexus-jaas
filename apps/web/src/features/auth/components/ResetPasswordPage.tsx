import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { resetPasswordSchema, type ResetPasswordFormData } from '../schemas/resetPasswordSchema'
import { useResetPassword } from '../hooks/useResetPassword'
import AuthLayout from '@components/layout/AuthLayout'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
  })

  const { mutate: resetPassword, isPending, error } = useResetPassword()

  const onSubmit = (data: ResetPasswordFormData) =>
    resetPassword({ token, newPassword: data.newPassword })

  const errorMessage =
    (error as { response?: { status?: number } })?.response?.status === 400
      ? 'O link de redefinição é inválido ou já expirou. Solicite um novo.'
      : error
        ? 'Erro ao redefinir senha. Tente novamente.'
        : null

  if (!token) {
    return (
      <AuthLayout>
        <p className="text-center text-sm text-destructive">Link de redefinição inválido.</p>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <h2 className="font-heading text-2xl">Redefinir senha</h2>

        {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}

        <div className="space-y-1">
          <label htmlFor="newPassword" className="text-xs text-muted-foreground">Nova senha</label>
          <Input {...register('newPassword')} id="newPassword" type="password" autoComplete="new-password" />
          {errors.newPassword && <p className="text-xs text-destructive">{errors.newPassword.message}</p>}
        </div>

        <div className="space-y-1">
          <label htmlFor="confirmPassword" className="text-xs text-muted-foreground">Confirmar nova senha</label>
          <Input {...register('confirmPassword')} id="confirmPassword" type="password" autoComplete="new-password" />
          {errors.confirmPassword && (
            <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
          )}
        </div>

        <Button type="submit" disabled={isPending} className="w-full">
          {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
          Redefinir senha
        </Button>
      </form>
    </AuthLayout>
  )
}

export default ResetPasswordPage
