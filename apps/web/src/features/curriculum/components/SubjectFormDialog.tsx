import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, X } from 'lucide-react'
import { subjectSchema, type SubjectFormData } from '../schemas/subjectSchema'
import type { Subject } from '../types'

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: SubjectFormData) => void
  isPending: boolean
  defaultValues?: Partial<Subject>
  title: string
}

function SubjectFormDialog({ open, onClose, onSubmit, isPending, defaultValues, title }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<SubjectFormData>({
    resolver: zodResolver(subjectSchema),
    defaultValues: {
      name: defaultValues?.name ?? '',
      code: defaultValues?.code ?? '',
      description: defaultValues?.description ?? '',
      workloadHours: defaultValues?.workloadHours ?? null,
    },
  })

  useEffect(() => {
    if (open) reset({
      name: defaultValues?.name ?? '',
      code: defaultValues?.code ?? '',
      description: defaultValues?.description ?? '',
      workloadHours: defaultValues?.workloadHours ?? null,
    })
  }, [open, defaultValues, reset])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-md rounded-lg border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-sm font-medium">Nome *</label>
            <input
              {...register('name')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: Matemática"
            />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Código</label>
            <input
              {...register('code')}
              className="w-full rounded border px-3 py-2 text-sm uppercase"
              placeholder="Ex: MAT01"
            />
            {errors.code && <p className="text-xs text-destructive">{errors.code.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Carga Horária (h)</label>
            <input
              {...register('workloadHours', { valueAsNumber: true })}
              type="number"
              min={1}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: 60"
            />
            {errors.workloadHours && <p className="text-xs text-destructive">{errors.workloadHours.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Descrição</label>
            <textarea
              {...register('description')}
              rows={3}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Descrição opcional"
            />
            {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
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

export default SubjectFormDialog
