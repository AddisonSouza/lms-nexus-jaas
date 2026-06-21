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
            <label className="text-sm font-medium">ID do usuário *</label>
            <input
              {...register('userId')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="UUID do membro"
            />
            {errors.userId && <p className="text-xs text-destructive">{errors.userId.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Papel *</label>
            <select {...register('role')} className="w-full rounded border px-3 py-2 text-sm">
              <option value="">Selecione...</option>
              <option value="PROFESSOR">Professor</option>
              <option value="ALUNO">Aluno</option>
            </select>
            {errors.role && <p className="text-xs text-destructive">{errors.role.message}</p>}
          </div>

          <DialogFooter>
            <button type="button" onClick={handleClose} className="rounded border px-4 py-2 text-sm">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
            >
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Adicionar
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AddMemberDialog
