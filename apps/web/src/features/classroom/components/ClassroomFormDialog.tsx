import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { classroomSchema, type ClassroomFormData } from '../schemas/classroomSchema'
import type { Classroom } from '../types'
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
  onSubmit: (data: ClassroomFormData) => void
  isPending: boolean
  defaultValues?: Partial<Classroom>
  title: string
}

function ClassroomFormDialog({ open, onClose, onSubmit, isPending, defaultValues, title }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<ClassroomFormData>({
    resolver: zodResolver(classroomSchema),
    defaultValues: {
      name: defaultValues?.name ?? '',
      description: defaultValues?.description ?? '',
      academicPeriod: defaultValues?.academicPeriod ?? '',
      status: defaultValues?.status,
    },
  })

  useEffect(() => {
    if (open) reset({
      name: defaultValues?.name ?? '',
      description: defaultValues?.description ?? '',
      academicPeriod: defaultValues?.academicPeriod ?? '',
      status: defaultValues?.status,
    })
  }, [open, defaultValues, reset])

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-sm font-medium">Nome *</label>
            <input
              {...register('name')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: Turma A — Manhã"
            />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Período Letivo *</label>
            <input
              {...register('academicPeriod')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: 2025/1"
            />
            {errors.academicPeriod && <p className="text-xs text-destructive">{errors.academicPeriod.message}</p>}
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

          {defaultValues && (
            <div className="space-y-1">
              <label className="text-sm font-medium">Status</label>
              <select {...register('status')} className="w-full rounded border px-3 py-2 text-sm">
                <option value="ACTIVE">Ativa</option>
                <option value="ARCHIVED">Arquivada</option>
              </select>
            </div>
          )}

          <DialogFooter>
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
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default ClassroomFormDialog
