import { useState } from 'react'
import { X, CheckCircle, Clock } from 'lucide-react'
import { useSubmissions } from '../hooks/useSubmissions'
import { useEvaluateSubmission } from '../hooks/useEvaluateSubmission'
import EvaluationDialog from './EvaluationDialog'
import type { Task, TaskSubmission } from '../types'
import type { EvaluationFormData } from '../schemas/evaluation.schema'

interface Props {
  open: boolean
  task: Task
  onClose: () => void
}

function SubmissionListDrawer({ open, task, onClose }: Props) {
  const [evaluating, setEvaluating] = useState<TaskSubmission | null>(null)
  const { data: submissions = [], isLoading } = useSubmissions(open ? task.id : '')
  const evaluate = useEvaluateSubmission(task.id)

  function handleEvaluate(data: EvaluationFormData) {
    if (!evaluating) return
    evaluate.mutate(
      {
        submissionId: evaluating.id,
        payload: { grade: data.grade ?? null, feedback: data.feedback },
      },
      { onSuccess: () => setEvaluating(null) },
    )
  }

  if (!open) return null

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/40" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 z-50 w-full max-w-lg overflow-y-auto bg-background shadow-xl">
        <div className="sticky top-0 flex items-center justify-between border-b bg-background px-6 py-4">
          <div>
            <h2 className="font-semibold">Submissões</h2>
            <p className="text-xs text-muted-foreground">{task.title}</p>
          </div>
          <button onClick={onClose} className="rounded p-1 hover:bg-accent">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="p-6">
          {isLoading && <p className="text-sm text-muted-foreground">Carregando...</p>}

          {!isLoading && submissions.length === 0 && (
            <p className="text-sm text-muted-foreground">Nenhuma submissão recebida ainda.</p>
          )}

          <div className="space-y-3">
            {submissions.map((sub) => (
              <div key={sub.id} className="rounded border p-4">
                <div className="mb-2 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    {sub.status === 'EVALUATED' ? (
                      <CheckCircle className="h-4 w-4 text-green-600" />
                    ) : (
                      <Clock className="h-4 w-4 text-yellow-500" />
                    )}
                    <span className="text-xs font-medium">
                      {sub.status === 'EVALUATED' ? 'Avaliada' : 'Aguardando avaliação'}
                    </span>
                  </div>
                  {sub.status === 'SUBMITTED' && (
                    <button
                      onClick={() => setEvaluating(sub)}
                      className="rounded border px-3 py-1 text-xs hover:bg-accent"
                    >
                      Avaliar
                    </button>
                  )}
                </div>

                {sub.textResponse && (
                  <p className="mb-2 line-clamp-2 text-sm text-muted-foreground">
                    {sub.textResponse}
                  </p>
                )}

                {sub.attachments.length > 0 && (
                  <p className="text-xs text-muted-foreground">
                    {sub.attachments.length} anexo(s)
                  </p>
                )}

                {sub.status === 'EVALUATED' && (
                  <div className="mt-2 rounded bg-muted/40 p-2 text-xs">
                    {sub.grade != null && (
                      <p>
                        <span className="font-medium">Nota:</span> {sub.grade}
                        {task.maxScore != null && ` / ${task.maxScore}`}
                      </p>
                    )}
                    {sub.feedback && (
                      <p>
                        <span className="font-medium">Feedback:</span> {sub.feedback}
                      </p>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      <EvaluationDialog
        open={!!evaluating}
        submission={evaluating}
        task={task}
        onClose={() => setEvaluating(null)}
        onSubmit={handleEvaluate}
        isPending={evaluate.isPending}
      />
    </>
  )
}

export default SubmissionListDrawer
