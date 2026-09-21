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
import { useOrgClassrooms } from '../hooks/useOrgClassrooms'

const schema = z.object({
  classroomId: z.string().min(1, 'Escolha uma turma'),
})

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (classroomId: string) => void
  isPending: boolean
  /** Turmas já vinculadas à disciplina — ficam fora da lista. */
  linkedClassroomIds: string[]
  /** Recusa da API (422 `CLASSROOM_ARCHIVED`, por exemplo). */
  error?: string | null
}

function LinkClassroomDialog({
  open,
  onClose,
  onSubmit,
  isPending,
  linkedClassroomIds,
  error,
}: Props) {
  // Só busca as turmas quando o diálogo abre — a página não precisa da lista.
  const { data: classrooms = [], isLoading } = useOrgClassrooms(open)

  // Turma arquivada não aceita vínculo (o back recusa com 422), e turma já
  // vinculada não tem o que escolher: nenhuma das duas entra na lista.
  const available = classrooms.filter(
    (c) => c.status === 'ACTIVE' && !linkedClassroomIds.includes(c.id),
  )

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { classroomId: '' },
  })

  const handleClose = () => { reset(); onClose() }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Vincular Turma</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((d) => onSubmit(d.classroomId))} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor="link-classroom-id" className="text-xs text-muted-foreground">
              Turma *
            </label>
            <select
              {...register('classroomId')}
              id="link-classroom-id"
              aria-invalid={!!errors.classroomId}
              disabled={isLoading || available.length === 0}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent disabled:opacity-60"
            >
              <option value="">Selecione...</option>
              {available.map((classroom) => (
                <option key={classroom.id} value={classroom.id}>
                  {classroom.name}
                </option>
              ))}
            </select>
            {errors.classroomId && (
              <p role="alert" className="text-xs text-destructive">{errors.classroomId.message}</p>
            )}
            {!isLoading && available.length === 0 && (
              <p className="text-xs text-muted-foreground">
                Nenhuma turma ativa disponível para vincular.
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
              Vincular
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default LinkClassroomDialog
