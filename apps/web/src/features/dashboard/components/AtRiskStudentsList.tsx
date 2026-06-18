import type { ClassroomHealth } from '../types'

interface Props {
  classrooms: ClassroomHealth[]
}

function AtRiskStudentsList({ classrooms }: Props) {
  const classroomsWithAtRiskStudents = classrooms.filter((c) => c.atRiskStudents.length > 0)

  if (classroomsWithAtRiskStudents.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhum aluno com pendências ou atrasos.</p>
  }

  return (
    <div className="space-y-4">
      {classroomsWithAtRiskStudents.map((classroom) => (
        <div key={classroom.classroomId}>
          <p className="mb-1 text-sm font-medium">{classroom.classroomName}</p>
          <ul className="space-y-1">
            {classroom.atRiskStudents.map((student) => (
              <li
                key={student.studentId}
                className="flex items-center justify-between border-b pb-1 text-sm"
              >
                <span>{student.studentName}</span>
                <span className="text-muted-foreground">{student.pendingCount} pendência(s)</span>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  )
}

export default AtRiskStudentsList
