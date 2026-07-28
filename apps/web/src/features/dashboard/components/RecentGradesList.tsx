import type { RecentGrade } from '../types'
import { Card } from '@components/ui/card'

interface Props {
  grades: RecentGrade[]
}

function RecentGradesList({ grades }: Props) {
  if (grades.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma nota recebida ainda.</p>
  }

  return (
    <div className="flex flex-col gap-2">
      {grades.map((grade) => (
        <Card key={grade.taskId} elevation="sm" className="flex-row items-start gap-3">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-accent-2-100 font-heading text-lg text-accent-2-800">
            {grade.grade}
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-sm font-semibold">{grade.title}</div>
            <div className="mb-1 text-xs text-muted-foreground">{grade.subjectName}</div>
            {grade.feedback && <p className="text-[13px] italic opacity-85">{grade.feedback}</p>}
          </div>
        </Card>
      ))}
    </div>
  )
}

export default RecentGradesList
