import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'
import { Button } from '@components/ui/button'
import { roleLabel } from '@lib/roles'
import { useTeacherCandidates } from '../hooks/useTeacherCandidates'

const schema = z.object({
  memberId: z.string().min(1, 'Escolha um membro'),
})

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (memberId: string) => void
  isPending: boolean
  /** Membros já atribuídos à disciplina — ficam fora da lista. */
  assignedMemberIds: string[]
  /** Recusa da API (422 `MEMBER_NOT_A_PROFESSOR`, por exemplo). */
  error?: string | null
}

function AssignTeacherDialog({
  open,
  onClose,
  onSubmit,
  isPending,
  assignedMemberIds,
  error,
}: Props) {
  // Só busca os membros quando o diálogo abre — a página não precisa da lista.
  const { data: candidates = [], isLoading } = useTeacherCandidates(open)

  const available = candidates.filter((m) => !assignedMemberIds.includes(m.id))

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { memberId: '' },
  })

  const handleClose = () => { reset(); onClose() }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Atribuir Professor</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((d) => onSubmit(d.memberId))} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor="assign-teacher-member" className="text-xs text-muted-foreground">
              Membro *
            </label>
            <select
              {...register('memberId')}
              id="assign-teacher-member"
              aria-invalid={!!errors.memberId}
              disabled={isLoading || available.length === 0}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent disabled:opacity-60"
            >
              <option value="">Selecione...</option>
              {available.map((member) => (
                <option key={member.id} value={member.id}>
                  {/* Sem o nome não dá para escolher; o papel separa homônimos. */}
                  {member.name ?? member.email ?? member.userId} ({roleLabel(member.role)})
                </option>
              ))}
            </select>
            {errors.memberId && (
              <p role="alert" className="text-xs text-destructive">{errors.memberId.message}</p>
            )}
            {!isLoading && available.length === 0 && (
              <p className="text-xs text-muted-foreground">
                Nenhum membro disponível para atribuir.
              </p>
            )}
          </div>

          {error && (
            <p role="alert" className="text-sm text-destructive">
              {error}
            </p>
          )}

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending || available.length === 0}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Atribuir
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AssignTeacherDialog
