import { Archive, BookOpen, CheckCircle, ClipboardList, UserPlus } from 'lucide-react'
import type { ActivityItem, ActivityType } from '../types'

interface Props {
  activity: ActivityItem[]
}

const ICONS: Record<ActivityType, typeof BookOpen> = {
  CLASSROOM_CREATED: BookOpen,
  CLASSROOM_ARCHIVED: Archive,
  TASK_CREATED: ClipboardList,
  TASK_EVALUATED: CheckCircle,
  MEMBER_JOINED: UserPlus,
}

function ActivityFeed({ activity }: Props) {
  if (activity.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma atividade no período selecionado.</p>
  }

  return (
    <ul className="space-y-2">
      {activity.map((item, index) => {
        const Icon = ICONS[item.type]
        return (
          <li key={`${item.type}-${item.referenceId}-${index}`} className="flex items-start gap-2 border-b pb-2 text-sm">
            <Icon className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
            <div>
              <p>{item.description}</p>
              <p className="text-xs text-muted-foreground">{new Date(item.occurredAt).toLocaleString('pt-BR')}</p>
            </div>
          </li>
        )
      })}
    </ul>
  )
}

export default ActivityFeed
