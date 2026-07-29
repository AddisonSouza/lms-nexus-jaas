import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { subjectSchema, type SubjectFormData } from '../schemas/subjectSchema'
import type { Subject } from '../types'
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

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Nome *</label>
            <Input {...register('name')} placeholder="Ex: Matemática" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Código</label>
            <Input {...register('code')} className="uppercase" placeholder="Ex: MAT01" />
            {errors.code && <p className="text-xs text-destructive">{errors.code.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Carga Horária (h)</label>
            <Input
              {...register('workloadHours', { valueAsNumber: true })}
              type="number"
              min={1}
              placeholder="Ex: 60"
            />
            {errors.workloadHours && <p className="text-xs text-destructive">{errors.workloadHours.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Descrição</label>
            <Textarea {...register('description')} rows={3} placeholder="Descrição opcional" />
            {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
          </div>

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

export default SubjectFormDialog
