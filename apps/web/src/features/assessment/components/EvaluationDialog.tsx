import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { evaluationSchema, type EvaluationFormData } from '../schemas/evaluation.schema'
import type { Task, TaskSubmission } from '../types'
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
import { CardKicker } from '@components/ui/card'

interface Props {
  open: boolean
  submission: TaskSubmission | null
  task: Task
  onClose: () => void
  onSubmit: (data: EvaluationFormData) => void
  isPending: boolean
}

function EvaluationDialog({ open, submission, task, onClose, onSubmit, isPending }: Props) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<EvaluationFormData>({
    resolver: zodResolver(evaluationSchema),
    defaultValues: { grade: null, feedback: '' },
  })

  useEffect(() => {
    if (open && submission) {
      reset({
        grade: submission.grade ?? null,
        feedback: submission.feedback ?? '',
      })
    }
  }, [open, submission, reset])

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Avaliar Submissão</DialogTitle>
        </DialogHeader>

        {submission && (
          <>
            {submission.textResponse && (
              <div className="rounded-[var(--radius-md)] bg-[color-mix(in_srgb,var(--color-text)_5%,transparent)] p-3 text-sm max-h-[150px] overflow-auto">
                <CardKicker>Resposta do aluno</CardKicker>
                <p className="mt-1 whitespace-pre-wrap">{submission.textResponse}</p>
              </div>
            )}

            {submission.attachments.length > 0 && (
              <div>
                <CardKicker>Anexos</CardKicker>
                <ul className="mt-1 space-y-1">
                  {submission.attachments.map((a) => (
                    <li key={a.id} className="text-xs text-accent">
                      {a.originalName}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {task.maxScore != null && (
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Nota (máx. {task.maxScore})</label>
              <Input
                type="number"
                step="0.01"
                min="0"
                max={task.maxScore}
                {...register('grade', { valueAsNumber: true })}
                className="w-32"
                placeholder="Ex: 8.5"
              />
              {errors.grade && <p className="text-xs text-destructive">{errors.grade.message}</p>}
            </div>
          )}

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Feedback *</label>
            <Textarea {...register('feedback')} rows={4} placeholder="Escreva o feedback para o aluno..." />
            {errors.feedback && <p className="text-xs text-destructive">{errors.feedback.message}</p>}
          </div>

          <div className="flex items-center gap-2 rounded-[var(--radius-md)] bg-accent-100 p-2.5 text-xs text-accent-800">
            A correção é definitiva — não há reabertura depois de salvar.
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Salvar Avaliação
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default EvaluationDialog
