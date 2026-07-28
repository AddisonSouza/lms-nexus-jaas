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
import { Input } from '@components/ui/input'
import { Textarea } from '@components/ui/textarea'
import { Button } from '@components/ui/button'

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
            <label className="text-xs text-muted-foreground">Nome *</label>
            <Input {...register('name')} placeholder="Ex: Turma A — Manhã" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Período Letivo *</label>
            <Input {...register('academicPeriod')} placeholder="Ex: 2025/1" />
            {errors.academicPeriod && <p className="text-xs text-destructive">{errors.academicPeriod.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Descrição</label>
            <Textarea {...register('description')} rows={3} placeholder="Descrição opcional" />
            {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
          </div>

          {defaultValues && (
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Status</label>
              <select
                {...register('status')}
                className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
              >
                <option value="ACTIVE">Ativa</option>
                <option value="ARCHIVED">Arquivada</option>
              </select>
            </div>
          )}

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Salvar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default ClassroomFormDialog
