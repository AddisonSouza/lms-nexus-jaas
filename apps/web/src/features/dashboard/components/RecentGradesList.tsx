import type { RecentGrade } from '../types'

interface Props {
  grades: RecentGrade[]
}

function RecentGradesList({ grades }: Props) {
  if (grades.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma nota recebida ainda.</p>
  }

  return (
    <ul className="space-y-2">
      {grades.map((grade) => (
        <li key={grade.taskId} className="border-b pb-2 text-sm">
          <div className="flex items-center justify-between">
            <div>
              <span>{grade.title}</span>
              <span className="ml-2 text-xs text-muted-foreground">{grade.subjectName}</span>
            </div>
            <span className="font-medium">{grade.grade}</span>
          </div>
          {grade.feedback && <p className="mt-1 text-xs text-muted-foreground">{grade.feedback}</p>}
        </li>
      ))}
    </ul>
  )
}

export default RecentGradesList
