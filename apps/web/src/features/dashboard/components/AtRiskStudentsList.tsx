import type { ClassroomHealth } from '../types'
import { Card } from '@components/ui/card'
import { Badge } from '@components/ui/badge'

interface Props {
  classrooms: ClassroomHealth[]
}

function AtRiskStudentsList({ classrooms }: Props) {
  const classroomsWithAtRiskStudents = classrooms.filter((c) => c.atRiskStudents.length > 0)

  if (classroomsWithAtRiskStudents.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhum aluno com pendências ou atrasos.</p>
  }

  return (
    <div className="flex flex-col gap-4">
      {classroomsWithAtRiskStudents.map((classroom) => (
        <div key={classroom.classroomId}>
          <p className="mb-1.5 text-sm font-semibold">{classroom.classroomName}</p>
          <Card elevation="sm" className="gap-1 p-2">
            <ul className="flex flex-col gap-0.5">
              {classroom.atRiskStudents.map((student) => (
                <li
                  key={student.studentId}
                  className="flex items-center justify-between rounded-full px-3 py-1.5 text-sm"
                >
                  <span>{student.studentName}</span>
                  <Badge variant="accent">{student.pendingCount} pendência(s)</Badge>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      ))}
    </div>
  )
}

export default AtRiskStudentsList
