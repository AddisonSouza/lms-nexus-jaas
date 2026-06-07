import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { resetPasswordSchema, type ResetPasswordFormData } from '../schemas/resetPasswordSchema'
import { useResetPassword } from '../hooks/useResetPassword'

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
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="w-full max-w-sm rounded-lg border p-6 shadow-sm text-center">
          <p className="text-sm text-destructive">Link de redefinição inválido.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="w-full max-w-sm space-y-4 rounded-lg border p-6 shadow-sm"
      >
        <h1 className="text-2xl font-semibold">Redefinir senha</h1>

        {errorMessage && (
          <p className="text-sm text-destructive">{errorMessage}</p>
        )}

        <div className="space-y-1">
          <label className="text-sm font-medium">Nova senha</label>
          <input
            {...register('newPassword')}
            type="password"
            autoComplete="new-password"
            className="w-full rounded border px-3 py-2 text-sm"
          />
          {errors.newPassword && (
            <p className="text-xs text-destructive">{errors.newPassword.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium">Confirmar nova senha</label>
          <input
            {...register('confirmPassword')}
            type="password"
            autoComplete="new-password"
            className="w-full rounded border px-3 py-2 text-sm"
          />
          {errors.confirmPassword && (
            <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isPending}
          className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
        >
          {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
          Redefinir senha
        </button>
      </form>
    </div>
  )
}

export default ResetPasswordPage
