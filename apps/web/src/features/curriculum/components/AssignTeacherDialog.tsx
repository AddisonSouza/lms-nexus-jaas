import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2, X } from 'lucide-react'

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

  if (!open) return null

  const handleClose = () => { reset(); onClose() }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-sm rounded-lg border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">Atribuir Professor</h2>
          <button onClick={handleClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit((d) => onSubmit(d.memberId))} className="space-y-4">
          <div className="space-y-1">
            <label className="text-sm font-medium">ID do Membro *</label>
            <input
              {...register('memberId')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="UUID do membro (PROFESSOR)"
            />
            {errors.memberId && <p className="text-xs text-destructive">{errors.memberId.message}</p>}
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={handleClose} className="rounded border px-4 py-2 text-sm">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
            >
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Atribuir
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AssignTeacherDialog
