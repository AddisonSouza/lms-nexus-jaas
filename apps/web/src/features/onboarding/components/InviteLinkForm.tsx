import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { extractInviteToken, inviteLinkSchema, type InviteLinkFormData } from '../schemas/inviteLinkSchema'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

function InviteLinkForm() {
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<InviteLinkFormData>({
    resolver: zodResolver(inviteLinkSchema),
  })

  // A validação real do convite (organização, papel, expiração) é da tela de
  // aceite; aqui só garantimos que há um token antes de mandar o usuário para lá.
  const onSubmit = ({ inviteLink }: InviteLinkFormData) => {
    const token = extractInviteToken(inviteLink)
    if (token) navigate(`/invitations/${token}/accept`)
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="w-full space-y-2">
      <label htmlFor="inviteLink" className="block text-xs text-muted-foreground">
        Link do convite
      </label>
      <Input
        id="inviteLink"
        {...register('inviteLink')}
        placeholder="Cole aqui o link recebido por e-mail"
        aria-invalid={!!errors.inviteLink}
      />
      {errors.inviteLink && (
        <p role="alert" className="text-xs text-destructive">
          {errors.inviteLink.message}
        </p>
      )}
      <Button type="submit" variant="secondary" className="w-full">
        Entrar na organização
      </Button>
    </form>
  )
}

export default InviteLinkForm
