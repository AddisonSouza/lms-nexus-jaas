import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { forgotPasswordSchema, type ForgotPasswordFormData } from '../schemas/forgotPasswordSchema'
import { useForgotPassword } from '../hooks/useForgotPassword'

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
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="w-full max-w-sm space-y-4 rounded-lg border p-6 shadow-sm text-center">
          <h1 className="text-2xl font-semibold">E-mail enviado</h1>
          <p className="text-sm text-muted-foreground">
            Se houver uma conta com esse e-mail, você receberá um link para redefinir sua senha em breve.
          </p>
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
        <h1 className="text-2xl font-semibold">Esqueci minha senha</h1>
        <p className="text-sm text-muted-foreground">
          Informe seu e-mail e enviaremos um link para redefinir sua senha.
        </p>

        <div className="space-y-1">
          <label htmlFor="email" className="text-sm font-medium">E-mail</label>
          <input
            {...register('email')}
            id="email"
            type="email"
            autoComplete="email"
            className="w-full rounded border px-3 py-2 text-sm"
          />
          {errors.email && (
            <p className="text-xs text-destructive">{errors.email.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isPending}
          className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
        >
          {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
          Enviar link
        </button>
      </form>
    </div>
  )
}

export default ForgotPasswordPage
