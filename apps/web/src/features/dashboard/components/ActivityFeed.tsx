import { Archive, BookOpen, CheckCircle, ClipboardList, UserPlus } from 'lucide-react'
import type { ActivityItem, ActivityType } from '../types'
import { Card } from '@components/ui/card'

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
    <Card elevation="sm" className="gap-3">
      {activity.map((item, index) => {
        const Icon = ICONS[item.type]
        return (
          <div key={`${item.type}-${item.referenceId}-${index}`} className="flex items-start gap-2.5 text-sm">
            <div className="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-accent-2-100 text-accent-2-800">
              <Icon className="h-3.5 w-3.5" />
            </div>
            <div>
              <p>{item.description}</p>
              <p className="text-xs text-muted-foreground">{new Date(item.occurredAt).toLocaleString('pt-BR')}</p>
            </div>
          </div>
        )
      })}
    </Card>
  )
}

export default ActivityFeed
