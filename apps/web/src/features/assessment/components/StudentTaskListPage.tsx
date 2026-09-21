import { useState } from 'react'
import { Send, Eye, CheckCircle, Clock, AlertCircle, Pencil, Lock } from 'lucide-react'
import { useStudentGrades } from '../hooks/useStudentGrades'
import { useSubmitTask, useEditSubmission } from '../hooks/useSubmitTask'
import SubmissionFormDialog from './SubmissionFormDialog'
import GradeFeedbackDrawer from './GradeFeedbackDrawer'
import type { TaskWithGrade } from '../types'
import type { SubmissionFormData } from '../schemas/submission.schema'
import ListErrorState from '@components/shared/ListErrorState'
import { apiErrorMessage } from '@lib/api-error'
import { Card } from '@components/ui/card'
import AttachmentLink from '@components/shared/AttachmentLink'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'

function StatusBadge({ task }: { task: TaskWithGrade }) {
  const { submission } = task
  if (!submission) {
    const isPast = new Date() > new Date(task.deadline)
    return (
      <Badge variant={isPast ? 'accent' : 'neutral'}>
        {isPast ? <AlertCircle className="mr-1 h-3 w-3" /> : <Clock className="mr-1 h-3 w-3" />}
        {isPast ? 'Não enviado (expirado)' : 'Não enviado'}
      </Badge>
    )
  }
  if (submission.status === 'EVALUATED') {
    return (
      <Badge variant="accent-2">
        <CheckCircle className="mr-1 h-3 w-3" />
        Avaliado
      </Badge>
    )
  }
  return (
    <Badge variant="neutral">
      <Clock className="mr-1 h-3 w-3" />
      Aguardando avaliação
    </Badge>
  )
}

function StudentTaskListPage() {
  const { data: tasks = [], isLoading, isError, error, isFetching, refetch } = useStudentGrades()
  const [submitting, setSubmitting] = useState<TaskWithGrade | null>(null)
  const [editing, setEditing] = useState<TaskWithGrade | null>(null)
  const [viewingGrade, setViewingGrade] = useState<TaskWithGrade | null>(null)
  const submitTask = useSubmitTask(submitting?.id ?? '')
  const editSubmission = useEditSubmission(editing?.id ?? '')

  function handleSubmit(data: SubmissionFormData) {
    if (!submitting) return
    submitTask.mutate(
      { taskId: submitting.id, textResponse: data.textResponse, files: data.files },
      { onSuccess: () => setSubmitting(null) }
    )
  }

  function handleEdit(data: SubmissionFormData) {
    if (!editing?.submission) return
    editSubmission.mutate(
      {
        taskId: editing.id,
        submissionId: editing.submission.id,
        textResponse: data.textResponse,
        files: data.files,
      },
      { onSuccess: () => setEditing(null) }
    )
  }

  if (isLoading) {
    return <div className="text-sm text-muted-foreground">Carregando tarefas...</div>
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="mb-1">Tarefas</h2>
        <p className="text-sm text-muted-foreground">Tarefas publicadas para entrega</p>
      </div>

      {isError ? (
        <ListErrorState subject="as tarefas" error={error} onRetry={() => void refetch()} isRetrying={isFetching} />
      ) : tasks.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma tarefa disponível no momento.</p>
      ) : (
        <div className="flex flex-col gap-2">
          {tasks.map((task) => {
            const isPastDeadline = new Date() > new Date(task.deadline)
            const hasSubmission = task.submission !== null
            const isEvaluated = task.submission?.status === 'EVALUATED'
            const canEdit = task.submission?.status === 'SUBMITTED' && !isPastDeadline

            return (
              <Card key={task.id} elevation="sm">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <p className="font-semibold">{task.title}</p>
                    <p className="text-xs text-muted-foreground">
                      Prazo: {new Date(task.deadline).toLocaleString('pt-BR')}
                    </p>
                    {task.maxScore != null && (
                      <p className="text-xs text-muted-foreground">Pontuação máxima: {task.maxScore}</p>
                    )}
                    {/* Os anexos do enunciado já vinham na resposta e nunca eram
                        renderizados: o aluno não tinha como chegar ao arquivo. */}
                    {task.attachments.length > 0 && (
                      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1">
                        {task.attachments.map((attachment) => (
                          <AttachmentLink
                            key={attachment.id}
                            fileKey={attachment.fileKey}
                            originalName={attachment.originalName}
                            sizeBytes={attachment.sizeBytes}
                          />
                        ))}
                      </div>
                    )}
                    {isEvaluated && task.submission?.grade != null && (
                      <p className="mt-1 text-sm font-semibold text-accent-2-700">
                        Nota: {task.submission.grade}
                        {task.maxScore != null && ` / ${task.maxScore}`}
                      </p>
                    )}
                  </div>

                  <div className="flex flex-col items-end gap-2">
                    <div className="flex items-center gap-2">
                      {/* O prazo encerra a tarefa; o badge ao lado segue contando
                          a entrega, que continua valendo depois de encerrada. */}
                      {task.status === 'CLOSED' && (
                        <Badge variant="neutral">
                          <Lock className="mr-1 h-3 w-3" />
                          Encerrada
                        </Badge>
                      )}
                      <StatusBadge task={task} />
                    </div>

                    <div className="flex gap-2">
                      {isEvaluated && (
                        <Button size="sm" variant="secondary" onClick={() => setViewingGrade(task)}>
                          <Eye className="h-3.5 w-3.5" />
                          Ver Nota
                        </Button>
                      )}

                      {canEdit && (
                        <Button size="sm" variant="secondary" onClick={() => setEditing(task)}>
                          <Pencil className="h-3.5 w-3.5" />
                          Editar resposta
                        </Button>
                      )}

                      {!hasSubmission && !isPastDeadline && (
                        <Button size="sm" onClick={() => setSubmitting(task)}>
                          <Send className="h-3.5 w-3.5" />
                          Enviar Resposta
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </Card>
            )
          })}
        </div>
      )}

      {submitting && (
        <SubmissionFormDialog
          open={true}
          taskTitle={submitting.title}
          deadline={submitting.deadline}
          onClose={() => { setSubmitting(null); submitTask.reset() }}
          onSubmit={handleSubmit}
          isPending={submitTask.isPending}
          error={submitTask.isError ? apiErrorMessage(submitTask.error) : null}
        />
      )}

      {editing && (
        <SubmissionFormDialog
          open={true}
          mode="edit"
          taskTitle={editing.title}
          deadline={editing.deadline}
          onClose={() => { setEditing(null); editSubmission.reset() }}
          onSubmit={handleEdit}
          isPending={editSubmission.isPending}
          error={editSubmission.isError ? apiErrorMessage(editSubmission.error) : null}
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
