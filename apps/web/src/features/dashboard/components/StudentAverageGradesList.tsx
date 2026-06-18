import type { StudentAverageGrade } from '../types'

interface Props {
  students: StudentAverageGrade[]
}

function StudentAverageGradesList({ students }: Props) {
  if (students.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma submissão avaliada ainda.</p>
  }

  return (
    <ul className="space-y-1">
      {students.map((student) => (
        <li key={student.studentId} className="flex items-center justify-between border-b pb-1 text-sm">
          <span>{student.studentName}</span>
          <span className="text-muted-foreground">{student.averageGrade}</span>
        </li>
      ))}
    </ul>
  )
}

export default StudentAverageGradesList
