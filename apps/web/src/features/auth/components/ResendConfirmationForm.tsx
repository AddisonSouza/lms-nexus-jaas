import { useState } from 'react'
import { Loader2, Mail } from 'lucide-react'
import { useResendConfirmation } from '../hooks/useResendConfirmation'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface ResendConfirmationFormProps {
  initialEmail?: string
}

function ResendConfirmationForm({ initialEmail = '' }: ResendConfirmationFormProps) {
  const [email, setEmail] = useState(initialEmail)
  const { mutate: resend, isPending, isSuccess } = useResendConfirmation()

  if (isSuccess) {
    return (
      <div className="mt-4 flex items-center gap-2 rounded-[var(--radius-md)] bg-accent-2-100 p-3 text-sm text-accent-2-800">
        <Mail className="h-4 w-4 shrink-0" />
        E-mail de confirmação reenviado. Verifique sua caixa de entrada.
      </div>
    )
  }

  return (
    <div className="mt-4 space-y-2 text-left">
      <p className="text-sm text-muted-foreground">Reenviar link de confirmação para:</p>
      <div className="flex gap-2">
        <Input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="seu@email.com"
          className="flex-1"
        />
        <Button type="button" disabled={isPending || !email} onClick={() => resend(email)}>
          {isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
          Reenviar
        </Button>
      </div>
    </div>
  )
}

export default ResendConfirmationForm
