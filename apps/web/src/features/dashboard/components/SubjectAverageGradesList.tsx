import type { SubjectAverageGrade } from '../types'
import { Card } from '@components/ui/card'

interface Props {
  subjects: SubjectAverageGrade[]
}

function SubjectAverageGradesList({ subjects }: Props) {
  if (subjects.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma média disponível ainda.</p>
  }

  return (
    <Card elevation="sm" className="gap-1 p-2">
      <ul className="flex flex-col gap-0.5">
        {subjects.map((subject) => (
          <li key={subject.subjectId} className="flex items-center justify-between rounded-full px-3 py-1.5 text-sm">
            <span>{subject.subjectName}</span>
            <span className="text-muted-foreground">{subject.averageGrade}</span>
          </li>
        ))}
      </ul>
    </Card>
  )
}

export default SubjectAverageGradesList
