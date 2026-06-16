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
} from '@features/components/ui/dialog'

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
              <div className="rounded border bg-muted/40 p-3 text-sm">
                <p className="mb-1 font-medium text-muted-foreground">Resposta do aluno:</p>
                <p className="whitespace-pre-wrap">{submission.textResponse}</p>
              </div>
            )}

            {submission.attachments.length > 0 && (
              <div>
                <p className="mb-1 text-xs font-medium text-muted-foreground">Anexos:</p>
                <ul className="space-y-1">
                  {submission.attachments.map((a) => (
                    <li key={a.id} className="text-xs text-primary">
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
            <div>
              <label className="mb-1 block text-sm font-medium">
                Nota (máx. {task.maxScore})
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                max={task.maxScore}
                {...register('grade', { valueAsNumber: true })}
                className="w-full rounded border px-3 py-2 text-sm"
                placeholder="Ex: 8.5"
              />
              {errors.grade && (
                <p className="mt-1 text-xs text-destructive">{errors.grade.message}</p>
              )}
            </div>
          )}

          <div>
            <label className="mb-1 block text-sm font-medium">Feedback *</label>
            <textarea
              {...register('feedback')}
              rows={4}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Escreva o feedback para o aluno..."
            />
            {errors.feedback && (
              <p className="mt-1 text-xs text-destructive">{errors.feedback.message}</p>
            )}
          </div>

          <DialogFooter>
            <button
              type="button"
              onClick={onClose}
              className="rounded border px-4 py-2 text-sm hover:bg-accent"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
            >
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Salvar Avaliação
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default EvaluationDialog
