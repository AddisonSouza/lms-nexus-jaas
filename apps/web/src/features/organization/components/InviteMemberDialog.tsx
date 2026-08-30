import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { inviteMemberSchema, type InviteMemberFormData } from '../schemas/inviteMemberSchema'
import { roleLabels } from '../roles'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@components/ui/dialog'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: InviteMemberFormData) => void
  isPending: boolean
  error: unknown
}

function InviteMemberDialog({ open, onClose, onSubmit, isPending, error }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<InviteMemberFormData>({
    resolver: zodResolver(inviteMemberSchema),
  })

  const handleClose = () => { reset(); onClose() }

  const status = (error as { response?: { status?: number } })?.response?.status
  const errorMessage =
    status === 409
      ? 'Esse e-mail já pertence a um membro desta organização.'
      : error
        ? 'Não foi possível enviar o convite. Tente novamente.'
        : null

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Convidar membro</DialogTitle>
          <DialogDescription>
            O convidado recebe um link por e-mail, válido por 7 dias.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {errorMessage && (
            <p role="alert" className="text-sm text-destructive">
              {errorMessage}
            </p>
          )}

          <div className="space-y-1">
            <label htmlFor="invite-email" className="text-xs text-muted-foreground">E-mail *</label>
            <Input
              {...register('email')}
              id="invite-email"
              type="email"
              placeholder="pessoa@escola.com"
              aria-invalid={!!errors.email}
            />
            {errors.email && (
              <p role="alert" className="text-xs text-destructive">{errors.email.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label htmlFor="invite-role" className="text-xs text-muted-foreground">Papel *</label>
            <select
              {...register('role')}
              id="invite-role"
              aria-invalid={!!errors.role}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
            >
              <option value="">Selecione...</option>
              <option value="GESTOR">{roleLabels.GESTOR}</option>
              <option value="PROFESSOR">{roleLabels.PROFESSOR}</option>
              <option value="ALUNO">{roleLabels.ALUNO}</option>
            </select>
            {errors.role && (
              <p role="alert" className="text-xs text-destructive">{errors.role.message}</p>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Enviar convite
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default InviteMemberDialog
