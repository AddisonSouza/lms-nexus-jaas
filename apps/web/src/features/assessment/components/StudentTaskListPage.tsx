import { useState } from 'react'
import { Send } from 'lucide-react'
import { useStudentTasks } from '../hooks/useStudentTasks'
import { useSubmitTask } from '../hooks/useSubmitTask'
import SubmissionFormDialog from './SubmissionFormDialog'
import type { Task } from '../types'
import type { SubmissionFormData } from '../schemas/submission.schema'

function StudentTaskListPage() {
  const { data: tasks = [], isLoading } = useStudentTasks()
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const submitTask = useSubmitTask(selectedTask?.id ?? '')

  function handleOpenDialog(task: Task) {
    setSelectedTask(task)
  }

  function handleSubmit(data: SubmissionFormData) {
    if (!selectedTask) return
    submitTask.mutate(
      { taskId: selectedTask.id, textResponse: data.textResponse, files: data.files },
      { onSuccess: () => setSelectedTask(null) }
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
            return (
              <div key={task.id} className="flex items-center justify-between rounded border p-4">
                <div>
                  <p className="font-medium">{task.title}</p>
                  <p className="text-xs text-muted-foreground">
                    Prazo: {new Date(task.deadline).toLocaleString('pt-BR')}
                    {isPastDeadline && (
                      <span className="ml-2 text-destructive font-medium">Expirado</span>
                    )}
                  </p>
                  {task.maxScore != null && (
                    <p className="text-xs text-muted-foreground">Pontuação: {task.maxScore}</p>
                  )}
                </div>
                <button
                  onClick={() => handleOpenDialog(task)}
                  className="flex items-center gap-1 rounded border px-3 py-1 text-xs hover:bg-accent disabled:opacity-50"
                >
                  <Send className="h-3 w-3" />
                  {isPastDeadline ? 'Ver Tarefa' : 'Enviar Resposta'}
                </button>
              </div>
            )
          })}
        </div>
      )}

      {selectedTask && (
        <SubmissionFormDialog
          open={true}
          taskTitle={selectedTask.title}
          deadline={selectedTask.deadline}
          onClose={() => setSelectedTask(null)}
          onSubmit={handleSubmit}
          isPending={submitTask.isPending}
        />
      )}
    </div>
  )
}

export default StudentTaskListPage
