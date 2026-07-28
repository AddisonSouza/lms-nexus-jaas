import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { addMemberSchema, type AddMemberFormData } from '../schemas/addMemberSchema'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: AddMemberFormData) => void
  isPending: boolean
}

function AddMemberDialog({ open, onClose, onSubmit, isPending }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<AddMemberFormData>({
    resolver: zodResolver(addMemberSchema),
  })

  const handleClose = () => { reset(); onClose() }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Adicionar membro</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">ID do usuário *</label>
            <Input {...register('userId')} placeholder="UUID do membro" />
            {errors.userId && <p className="text-xs text-destructive">{errors.userId.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Papel *</label>
            <select
              {...register('role')}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
            >
              <option value="">Selecione...</option>
              <option value="PROFESSOR">Professor</option>
              <option value="ALUNO">Aluno</option>
            </select>
            {errors.role && <p className="text-xs text-destructive">{errors.role.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Adicionar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AddMemberDialog
