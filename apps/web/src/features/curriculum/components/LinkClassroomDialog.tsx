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
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

const schema = z.object({
  classroomId: z.string().uuid('ID de turma inválido'),
})

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (classroomId: string) => void
  isPending: boolean
}

function LinkClassroomDialog({ open, onClose, onSubmit, isPending }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
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
            <label className="text-xs text-muted-foreground">ID da Turma *</label>
            <Input {...register('classroomId')} placeholder="UUID da turma" />
            {errors.classroomId && <p className="text-xs text-destructive">{errors.classroomId.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
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
