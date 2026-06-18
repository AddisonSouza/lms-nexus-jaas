import type { StudentSummary } from '../types'

interface Props {
  students: StudentSummary[]
}

function StudentsWithoutSubmissionList({ students }: Props) {
  if (students.length === 0) {
    return <p className="text-sm text-muted-foreground">Todos os alunos entregaram a última tarefa.</p>
  }

  return (
    <ul className="space-y-1">
      {students.map((student) => (
        <li key={student.studentId} className="border-b pb-1 text-sm">
          {student.studentName}
        </li>
      ))}
    </ul>
  )
}

export default StudentsWithoutSubmissionList
