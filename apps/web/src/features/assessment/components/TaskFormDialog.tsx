import { useEffect, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { taskSchema, type TaskFormData } from '../schemas/task.schema'
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
  subjectId: string
  onClose: () => void
  onSubmit: (data: TaskFormData) => void
  isPending: boolean
}

function TaskFormDialog({ open, subjectId, onClose, onSubmit, isPending }: Props) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<TaskFormData>({
    resolver: zodResolver(taskSchema),
    defaultValues: { subjectId, title: '', description: '', deadline: '', maxScore: null, files: [] },
  })

  useEffect(() => {
    if (open) {
      reset({ subjectId, title: '', description: '', deadline: '', maxScore: null, files: [] })
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [open, subjectId, reset])

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Nova Tarefa</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <input type="hidden" {...register('subjectId')} />

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Título *</label>
            <Input {...register('title')} placeholder="Ex: Lista de exercícios 01" />
            {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Enunciado * (Markdown suportado)</label>
            <Textarea {...register('description')} rows={5} className="font-mono" placeholder="Descreva a tarefa em Markdown..." />
            {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label htmlFor="deadline" className="text-xs text-muted-foreground">Prazo *</label>
              <Input id="deadline" {...register('deadline')} type="datetime-local" />
              {errors.deadline && <p className="text-xs text-destructive">{errors.deadline.message}</p>}
            </div>

            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Pontuação máxima</label>
              <Input {...register('maxScore', { valueAsNumber: true })} type="number" min={0} step={0.5} placeholder="Ex: 10" />
              {errors.maxScore && <p className="text-xs text-destructive">{errors.maxScore.message}</p>}
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Materiais de apoio</label>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept=".pdf,.doc,.docx,.zip,.jpg,.jpeg,.png"
              onChange={(e) => setValue('files', Array.from(e.target.files ?? []))}
              className="w-full rounded-[var(--radius-md)] border border-border bg-surface px-3 py-2 text-sm file:mr-2 file:rounded-full file:border-0 file:bg-accent-100 file:px-3 file:py-1 file:text-accent-800"
            />
            <p className="text-xs text-muted-foreground">PDF, DOC, DOCX, ZIP, JPG, PNG — máx. 50MB cada</p>
            {errors.files && <p className="text-xs text-destructive">{errors.files.message}</p>}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Criar Tarefa
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default TaskFormDialog
