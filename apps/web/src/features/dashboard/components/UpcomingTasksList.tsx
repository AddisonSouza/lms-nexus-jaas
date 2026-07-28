import type { UpcomingTask } from '../types'
import { Card } from '@components/ui/card'

interface Props {
  tasks: UpcomingTask[]
}

function UpcomingTasksList({ tasks }: Props) {
  if (tasks.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma tarefa pendente.</p>
  }

  return (
    <Card elevation="sm" className="gap-1 p-2">
      <ul className="flex flex-col gap-0.5">
        {tasks.map((task) => (
          <li key={task.taskId} className="flex items-center justify-between rounded-full px-3 py-2 text-sm">
            <div>
              <span>{task.title}</span>
              <span className="ml-2 text-xs text-muted-foreground">{task.subjectName}</span>
            </div>
            <span className="text-muted-foreground">{new Date(task.deadline).toLocaleDateString('pt-BR')}</span>
          </li>
        ))}
      </ul>
    </Card>
  )
}

export default UpcomingTasksList
