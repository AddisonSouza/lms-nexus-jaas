import { useState } from 'react'
import { Plus, Send, Users } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useCreateTask } from '../hooks/useCreateTask'
import { usePublishTask } from '../hooks/usePublishTask'
import { useSubjectList } from '../hooks/useSubjectList'
import { taskKeys } from '../api/query-keys'
import { listTasks } from '../api/tasks'
import TaskFormDialog from './TaskFormDialog'
import SubmissionListDrawer from './SubmissionListDrawer'
import type { Task, TaskStatus } from '../types'
import type { TaskFormData } from '../schemas/task.schema'
import { Card } from '@components/ui/card'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'

// A lista mostrava o enum cru ("PUBLISHED"). Encerrada é derivada do prazo no
// back-end, então chega aqui como qualquer outro status.
const TASK_STATUS_LABEL: Record<TaskStatus, string> = {
  DRAFT: 'Rascunho',
  PUBLISHED: 'Publicada',
  CLOSED: 'Encerrada',
  GRADED: 'Avaliada',
}

function publishErrorMessage(error: unknown): string {
  const status = (error as { response?: { status?: number } })?.response?.status
  if (status === 409) return 'Esta tarefa já está publicada. Recarregue a lista para ver o status atual.'
  if (status === 403) return 'Você não tem permissão para publicar esta tarefa.'
  return 'Não foi possível publicar a tarefa. Tente de novo em instantes.'
}

function TaskListPage() {
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedSubjectId, setSelectedSubjectId] = useState('')
  const [submissionsTask, setSubmissionsTask] = useState<Task | null>(null)

  const { data: subjects = [] } = useSubjectList()

  const { data: tasks = [] } = useQuery<Task[]>({
    queryKey: taskKeys.lists(),
    queryFn: listTasks,
  })

  const createTask = useCreateTask()
  const publishTask = usePublishTask()

  function handleSubmit(data: TaskFormData) {
    createTask.mutate(
      {
        subjectId: data.subjectId,
        title: data.title,
        description: data.description,
        deadline: data.deadline,
        maxScore: data.maxScore,
        files: data.files,
      },
      { onSuccess: () => setDialogOpen(false) }
    )
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="mb-0">Tarefas</h2>
        <div className="flex items-center gap-3">
          <select
            value={selectedSubjectId}
            onChange={(e) => setSelectedSubjectId(e.target.value)}
            className="h-9 rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
          >
            <option value="">Selecione uma disciplina</option>
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <Button onClick={() => setDialogOpen(true)} disabled={!selectedSubjectId}>
            <Plus className="h-4 w-4" />
            Nova Tarefa
          </Button>
        </div>
      </div>

      {publishTask.isError && (
        <p className="mb-3 text-sm text-destructive">{publishErrorMessage(publishTask.error)}</p>
      )}

      {tasks.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma tarefa criada ainda.</p>
      ) : (
        <div className="flex flex-col gap-2">
          {tasks.map((task) => (
            <Card key={task.id} elevation="sm" className="flex-row items-center justify-between">
              <div>
                <p className="font-semibold">{task.title}</p>
                <p className="mt-0.5 flex items-center gap-2 text-xs text-muted-foreground">
                  Prazo: {new Date(task.deadline).toLocaleString('pt-BR', {
                    dateStyle: 'short',
                    timeStyle: 'short',
                  })}
                  <Badge variant={task.status === 'PUBLISHED' ? 'accent-2' : 'neutral'}>
                    {TASK_STATUS_LABEL[task.status]}
                  </Badge>
                </p>
              </div>
              <div className="flex items-center gap-2">
                {/* Encerrada também: é justamente depois do prazo que o professor
                    precisa alcançar as respostas para avaliar. */}
                {(task.status === 'PUBLISHED' || task.status === 'CLOSED') && (
                  <Button size="sm" variant="secondary" onClick={() => setSubmissionsTask(task)}>
                    <Users className="h-3.5 w-3.5" />
                    Ver Submissões
                  </Button>
                )}
                {task.status === 'DRAFT' && (
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => publishTask.mutate(task.id)}
                    disabled={publishTask.isPending}
                  >
                    <Send className="h-3.5 w-3.5" />
                    Publicar
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <TaskFormDialog
        open={dialogOpen}
        subjectId={selectedSubjectId}
        onClose={() => setDialogOpen(false)}
        onSubmit={handleSubmit}
        isPending={createTask.isPending}
      />

      {submissionsTask && (
        <SubmissionListDrawer
          open={!!submissionsTask}
          task={submissionsTask}
          onClose={() => setSubmissionsTask(null)}
        />
      )}
    </div>
  )
}

export default TaskListPage
