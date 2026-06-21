import { useState } from 'react'
import { Send, Eye, CheckCircle, Clock, AlertCircle } from 'lucide-react'
import { useStudentGrades } from '../hooks/useStudentGrades'
import { useSubmitTask } from '../hooks/useSubmitTask'
import SubmissionFormDialog from './SubmissionFormDialog'
import GradeFeedbackDrawer from './GradeFeedbackDrawer'
import type { TaskWithGrade } from '../types'
import type { SubmissionFormData } from '../schemas/submission.schema'

function StatusBadge({ task }: { task: TaskWithGrade }) {
  const { submission } = task
  if (!submission) {
    const isPast = new Date() > new Date(task.deadline)
    return (
      <span className={`flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${isPast ? 'bg-destructive/10 text-destructive' : 'bg-muted text-muted-foreground'}`}>
        {isPast ? <AlertCircle className="h-3 w-3" /> : <Clock className="h-3 w-3" />}
        {isPast ? 'Não enviado (expirado)' : 'Não enviado'}
      </span>
    )
  }
  if (submission.status === 'EVALUATED') {
    return (
      <span className="flex items-center gap-1 rounded-full bg-success/10 px-2 py-0.5 text-xs font-medium text-success">
        <CheckCircle className="h-3 w-3" />
        Avaliado
      </span>
    )
  }
  return (
    <span className="flex items-center gap-1 rounded-full bg-warning/10 px-2 py-0.5 text-xs font-medium text-warning">
      <Clock className="h-3 w-3" />
      Aguardando avaliação
    </span>
  )
}

function StudentTaskListPage() {
  const { data: tasks = [], isLoading } = useStudentGrades()
  const [submitting, setSubmitting] = useState<TaskWithGrade | null>(null)
  const [viewingGrade, setViewingGrade] = useState<TaskWithGrade | null>(null)
  const submitTask = useSubmitTask(submitting?.id ?? '')

  function handleSubmit(data: SubmissionFormData) {
    if (!submitting) return
    submitTask.mutate(
      { taskId: submitting.id, textResponse: data.textResponse, files: data.files },
      { onSuccess: () => setSubmitting(null) }
    )
  }

  if (isLoading) {
    return <div className="p-6 text-sm text-muted-foreground">Carregando tarefas...</div>
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Tarefas</h1>
        <p className="mt-1 text-sm text-muted-foreground">Tarefas publicadas para entrega</p>
      </div>

      {tasks.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma tarefa disponível no momento.</p>
      ) : (
        <div className="space-y-2">
          {tasks.map((task) => {
            const isPastDeadline = new Date() > new Date(task.deadline)
            const hasSubmission = task.submission !== null
            const isEvaluated = task.submission?.status === 'EVALUATED'

            return (
              <div key={task.id} className="rounded border p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <p className="font-medium">{task.title}</p>
                    <p className="text-xs text-muted-foreground">
                      Prazo: {new Date(task.deadline).toLocaleString('pt-BR')}
                    </p>
                    {task.maxScore != null && (
                      <p className="text-xs text-muted-foreground">Pontuação máxima: {task.maxScore}</p>
                    )}
                    {isEvaluated && task.submission?.grade != null && (
                      <p className="mt-1 text-sm font-medium text-success">
                        Nota: {task.submission.grade}
                        {task.maxScore != null && ` / ${task.maxScore}`}
                      </p>
                    )}
                  </div>

                  <div className="flex flex-col items-end gap-2">
                    <StatusBadge task={task} />

                    <div className="flex gap-2">
                      {isEvaluated && (
                        <button
                          onClick={() => setViewingGrade(task)}
                          className="flex items-center gap-1 rounded border px-3 py-1 text-xs hover:bg-accent"
                        >
                          <Eye className="h-3 w-3" />
                          Ver Nota
                        </button>
                      )}

                      {!hasSubmission && !isPastDeadline && (
                        <button
                          onClick={() => setSubmitting(task)}
                          className="flex items-center gap-1 rounded border px-3 py-1 text-xs hover:bg-accent"
                        >
                          <Send className="h-3 w-3" />
                          Enviar Resposta
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {submitting && (
        <SubmissionFormDialog
          open={true}
          taskTitle={submitting.title}
          deadline={submitting.deadline}
          onClose={() => setSubmitting(null)}
          onSubmit={handleSubmit}
          isPending={submitTask.isPending}
        />
      )}

      {viewingGrade && (
        <GradeFeedbackDrawer
          open={true}
          task={viewingGrade}
          onClose={() => setViewingGrade(null)}
        />
      )}
    </div>
  )
}

export default StudentTaskListPage
