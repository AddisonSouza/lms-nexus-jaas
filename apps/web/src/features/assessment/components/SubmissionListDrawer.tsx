import { useState } from 'react'
import { CheckCircle, Clock } from 'lucide-react'
import { useSubmissions } from '../hooks/useSubmissions'
import { useEvaluateSubmission } from '../hooks/useEvaluateSubmission'
import EvaluationDialog from './EvaluationDialog'
import type { Task, TaskSubmission } from '../types'
import type { EvaluationFormData } from '../schemas/evaluation.schema'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from '@components/ui/sheet'
import { Card } from '@components/ui/card'
import { Button } from '@components/ui/button'

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

  return (
    <>
      <Sheet open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
        <SheetContent side="right" className="w-full max-w-lg overflow-y-auto">
          <SheetHeader>
            <SheetTitle>Submissões</SheetTitle>
            <SheetDescription>{task.title}</SheetDescription>
          </SheetHeader>

          <div className="flex flex-col gap-3 px-4 pb-4">
            {isLoading && <p className="text-sm text-muted-foreground">Carregando...</p>}

            {!isLoading && submissions.length === 0 && (
              <p className="text-sm text-muted-foreground">Nenhuma submissão recebida ainda.</p>
            )}

            {submissions.map((sub) => (
              <Card key={sub.id} elevation="sm">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    {sub.status === 'EVALUATED' ? (
                      <CheckCircle className="h-4 w-4 text-accent-2-700" />
                    ) : (
                      <Clock className="h-4 w-4 text-muted-foreground" />
                    )}
                    <span className="text-xs font-semibold">
                      {sub.status === 'EVALUATED' ? 'Avaliada' : 'Aguardando avaliação'}
                    </span>
                  </div>
                  {(sub.status === 'SUBMITTED' || sub.status === 'LATE') && (
                    <Button size="sm" variant="secondary" onClick={() => setEvaluating(sub)}>
                      Avaliar
                    </Button>
                  )}
                </div>

                {sub.textResponse && (
                  <p className="line-clamp-2 text-sm text-muted-foreground">
                    {sub.textResponse}
                  </p>
                )}

                {sub.attachments.length > 0 && (
                  <p className="text-xs text-muted-foreground">
                    {sub.attachments.length} anexo(s)
                  </p>
                )}

                {sub.status === 'EVALUATED' && (
                  <div className="rounded-[var(--radius-md)] bg-[color-mix(in_srgb,var(--color-text)_5%,transparent)] p-2 text-xs">
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
              </Card>
            ))}
          </div>
        </SheetContent>
      </Sheet>

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
