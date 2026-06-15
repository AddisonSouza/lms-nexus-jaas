import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, X } from 'lucide-react'
import { topicSchema, type TopicFormData } from '../schemas/topicSchema'

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: TopicFormData) => void
  isPending: boolean
  defaultValues?: { title: string }
  title: string
}

function TopicFormDialog({ open, onClose, onSubmit, isPending, defaultValues, title }: Props) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TopicFormData>({
    resolver: zodResolver(topicSchema),
    defaultValues: { title: defaultValues?.title ?? '' },
  })

  useEffect(() => {
    if (open) reset({ title: defaultValues?.title ?? '' })
  }, [open, defaultValues, reset])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-sm rounded-lg border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-sm font-medium">Título *</label>
            <input
              {...register('title')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: Introdução"
              autoFocus
            />
            {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={onClose} className="rounded border px-4 py-2 text-sm">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
            >
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Salvar
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default TopicFormDialog
