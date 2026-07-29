import type { StudentSummary } from '../types'
import { Card } from '@components/ui/card'

interface Props {
  students: StudentSummary[]
}

function StudentsWithoutSubmissionList({ students }: Props) {
  if (students.length === 0) {
    return <p className="text-sm text-muted-foreground">Todos os alunos entregaram a última tarefa.</p>
  }

  return (
    <Card elevation="sm" className="gap-1 p-2">
      <ul className="flex flex-col gap-0.5">
        {students.map((student) => (
          <li key={student.studentId} className="rounded-full px-3 py-1.5 text-sm">
            {student.studentName}
          </li>
        ))}
      </ul>
    </Card>
  )
}

export default StudentsWithoutSubmissionList
