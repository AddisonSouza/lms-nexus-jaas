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
  memberId: z.string().uuid('ID de membro inválido'),
})

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (memberId: string) => void
  isPending: boolean
}

function AssignTeacherDialog({ open, onClose, onSubmit, isPending }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
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
            <label className="text-xs text-muted-foreground">ID do Membro *</label>
            <Input {...register('memberId')} placeholder="UUID do membro (PROFESSOR)" />
            {errors.memberId && <p className="text-xs text-destructive">{errors.memberId.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
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
