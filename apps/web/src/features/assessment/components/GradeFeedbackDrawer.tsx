import { CheckCircle, AlertCircle, Clock } from 'lucide-react'
import type { TaskWithGrade } from '../types'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from '@components/ui/sheet'
import { Card, CardKicker } from '@components/ui/card'
import { Badge } from '@components/ui/badge'

interface Props {
  open: boolean
  task: TaskWithGrade
  onClose: () => void
}

function GradeFeedbackDrawer({ open, task, onClose }: Props) {
  const { submission } = task
  const isEvaluated = submission?.status === 'EVALUATED'

  return (
    <Sheet open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <SheetContent side="right" className="w-full max-w-lg overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Nota e Feedback</SheetTitle>
          <SheetDescription>{task.title}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-col gap-3 px-4 pb-4">
          <div className="flex items-center gap-2">
            {isEvaluated ? (
              <CheckCircle className="h-5 w-5 text-accent-2-700" />
            ) : (
              <Clock className="h-5 w-5 text-accent" />
            )}
            <span className="text-sm font-semibold">
              {isEvaluated ? 'Avaliada' : 'Aguardando avaliação'}
            </span>
            {submission?.lateSubmission && (
              <Badge variant="accent" className="ml-auto">
                <AlertCircle className="mr-1 h-3 w-3" />
                Atrasado
              </Badge>
            )}
          </div>

          {isEvaluated && submission && (
            <>
              {submission.grade != null && (
                <Card elevation="sm">
                  <CardKicker>Nota</CardKicker>
                  <div className="flex items-baseline gap-1">
                    <span className="font-heading text-3xl">{submission.grade}</span>
                    {task.maxScore != null && (
                      <span className="text-sm text-muted-foreground">/ {task.maxScore}</span>
                    )}
                  </div>
                </Card>
              )}

              {submission.feedback && (
                <Card elevation="sm">
                  <CardKicker>Feedback do professor</CardKicker>
                  <p className="text-sm leading-relaxed">{submission.feedback}</p>
                </Card>
              )}
            </>
          )}

          <Card elevation="sm" className="text-xs text-muted-foreground">
            <div className="flex justify-between">
              <span>Prazo</span>
              <span>{new Date(task.deadline).toLocaleString('pt-BR')}</span>
            </div>
            {submission?.submittedAt && (
              <div className="flex justify-between">
                <span>Enviado em</span>
                <span>{new Date(submission.submittedAt).toLocaleString('pt-BR')}</span>
              </div>
            )}
          </Card>
        </div>
      </SheetContent>
    </Sheet>
  )
}

export default GradeFeedbackDrawer
