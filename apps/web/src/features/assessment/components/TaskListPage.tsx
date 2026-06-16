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
import type { Task } from '../types'
import type { TaskFormData } from '../schemas/task.schema'

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
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Tarefas</h1>
        <div className="flex items-center gap-3">
          <select
            value={selectedSubjectId}
            onChange={(e) => setSelectedSubjectId(e.target.value)}
            className="rounded border px-3 py-2 text-sm"
          >
            <option value="">Selecione uma disciplina</option>
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <button
            onClick={() => setDialogOpen(true)}
            disabled={!selectedSubjectId}
            className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
          >
            <Plus className="h-4 w-4" />
            Nova Tarefa
          </button>
        </div>
      </div>

      {tasks.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma tarefa criada ainda.</p>
      ) : (
        <div className="space-y-2">
          {tasks.map((task) => (
            <div key={task.id} className="flex items-center justify-between rounded border p-4">
              <div>
                <p className="font-medium">{task.title}</p>
                <p className="text-xs text-muted-foreground">
                  Prazo: {new Date(task.deadline).toLocaleString('pt-BR')} · Status:{' '}
                  <span className={task.status === 'PUBLISHED' ? 'text-green-600' : 'text-yellow-600'}>
                    {task.status}
                  </span>
                </p>
              </div>
              <div className="flex items-center gap-2">
                {task.status === 'PUBLISHED' && (
                  <button
                    onClick={() => setSubmissionsTask(task)}
                    className="flex items-center gap-1 rounded border px-3 py-1 text-xs hover:bg-accent"
                  >
                    <Users className="h-3 w-3" />
                    Ver Submissões
                  </button>
                )}
                {task.status === 'DRAFT' && (
                  <button
                    onClick={() => publishTask.mutate(task.id)}
                    disabled={publishTask.isPending}
                    className="flex items-center gap-1 rounded border px-3 py-1 text-xs hover:bg-accent disabled:opacity-50"
                  >
                    <Send className="h-3 w-3" />
                    Publicar
                  </button>
                )}
              </div>
            </div>
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
