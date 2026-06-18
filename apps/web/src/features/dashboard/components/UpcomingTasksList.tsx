import type { UpcomingTask } from '../types'

interface Props {
  tasks: UpcomingTask[]
}

function UpcomingTasksList({ tasks }: Props) {
  if (tasks.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma tarefa pendente.</p>
  }

  return (
    <ul className="space-y-1">
      {tasks.map((task) => (
        <li key={task.taskId} className="flex items-center justify-between border-b pb-1 text-sm">
          <div>
            <span>{task.title}</span>
            <span className="ml-2 text-xs text-muted-foreground">{task.subjectName}</span>
          </div>
          <span className="text-muted-foreground">{new Date(task.deadline).toLocaleDateString('pt-BR')}</span>
        </li>
      ))}
    </ul>
  )
}

export default UpcomingTasksList
