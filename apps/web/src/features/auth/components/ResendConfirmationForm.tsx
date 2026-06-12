import { useState } from 'react'
import { Loader2, Mail } from 'lucide-react'
import { useResendConfirmation } from '../hooks/useResendConfirmation'

interface ResendConfirmationFormProps {
  initialEmail?: string
}

function ResendConfirmationForm({ initialEmail = '' }: ResendConfirmationFormProps) {
  const [email, setEmail] = useState(initialEmail)
  const { mutate: resend, isPending, isSuccess } = useResendConfirmation()

  if (isSuccess) {
    return (
      <div className="mt-4 rounded border border-green-200 bg-green-50 p-3 text-center text-sm text-green-700">
        <Mail className="mx-auto mb-1 h-4 w-4" />
        E-mail de confirmação reenviado. Verifique sua caixa de entrada.
      </div>
    )
  }

  return (
    <div className="mt-4 space-y-2">
      <p className="text-sm text-muted-foreground">Reenviar link de confirmação para:</p>
      <div className="flex gap-2">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="seu@email.com"
          className="flex-1 rounded border px-3 py-2 text-sm"
        />
        <button
          type="button"
          disabled={isPending || !email}
          onClick={() => resend(email)}
          className="flex items-center gap-1 rounded bg-primary px-3 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
        >
          {isPending && <Loader2 className="h-3 w-3 animate-spin" />}
          Reenviar
        </button>
      </div>
    </div>
  )
}

export default ResendConfirmationForm
