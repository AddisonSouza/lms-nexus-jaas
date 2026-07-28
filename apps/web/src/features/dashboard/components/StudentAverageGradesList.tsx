import type { StudentAverageGrade } from '../types'
import { Card } from '@components/ui/card'

interface Props {
  students: StudentAverageGrade[]
}

function StudentAverageGradesList({ students }: Props) {
  if (students.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma submissão avaliada ainda.</p>
  }

  return (
    <Card elevation="sm" className="gap-1 p-2">
      <ul className="flex flex-col gap-0.5">
        {students.map((student) => (
          <li key={student.studentId} className="flex items-center justify-between rounded-full px-3 py-1.5 text-sm">
            <span>{student.studentName}</span>
            <span className="text-muted-foreground">{student.averageGrade}</span>
          </li>
        ))}
      </ul>
    </Card>
  )
}

export default StudentAverageGradesList
