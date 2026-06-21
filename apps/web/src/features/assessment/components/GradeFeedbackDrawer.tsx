import { X, CheckCircle, AlertCircle, Clock } from 'lucide-react'
import type { TaskWithGrade } from '../types'

interface Props {
  open: boolean
  task: TaskWithGrade
  onClose: () => void
}

function GradeFeedbackDrawer({ open, task, onClose }: Props) {
  if (!open) return null

  const { submission } = task
  const isEvaluated = submission?.status === 'EVALUATED'

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/40" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 z-50 w-full max-w-lg overflow-y-auto bg-background shadow-xl">
        <div className="sticky top-0 flex items-center justify-between border-b bg-background px-6 py-4">
          <div>
            <h2 className="font-semibold">Nota e Feedback</h2>
            <p className="text-xs text-muted-foreground">{task.title}</p>
          </div>
          <button onClick={onClose} className="rounded p-1 hover:bg-accent">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-4 p-6">
          <div className="flex items-center gap-2">
            {isEvaluated ? (
              <CheckCircle className="h-5 w-5 text-success" />
            ) : (
              <Clock className="h-5 w-5 text-warning" />
            )}
            <span className="text-sm font-medium">
              {isEvaluated ? 'Avaliada' : 'Aguardando avaliação'}
            </span>
            {submission?.lateSubmission && (
              <span className="ml-auto flex items-center gap-1 rounded-full bg-destructive/10 px-2 py-0.5 text-xs font-medium text-destructive">
                <AlertCircle className="h-3 w-3" />
                Atrasado
              </span>
            )}
          </div>

          {isEvaluated && submission && (
            <>
              {submission.grade != null && (
                <div className="rounded border p-4">
                  <p className="mb-1 text-xs text-muted-foreground">Nota</p>
                  <div className="flex items-baseline gap-1">
                    <span className="text-3xl font-bold">{submission.grade}</span>
                    {task.maxScore != null && (
                      <span className="text-sm text-muted-foreground">/ {task.maxScore}</span>
                    )}
                  </div>
                </div>
              )}

              {submission.feedback && (
                <div className="rounded border p-4">
                  <p className="mb-2 text-xs font-medium text-muted-foreground">Feedback do professor</p>
                  <p className="text-sm leading-relaxed">{submission.feedback}</p>
                </div>
              )}
            </>
          )}

          <div className="rounded border p-4 text-xs text-muted-foreground">
            <div className="flex justify-between">
              <span>Prazo</span>
              <span>{new Date(task.deadline).toLocaleString('pt-BR')}</span>
            </div>
            {submission?.submittedAt && (
              <div className="mt-1 flex justify-between">
                <span>Enviado em</span>
                <span>{new Date(submission.submittedAt).toLocaleString('pt-BR')}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default GradeFeedbackDrawer
