import type { ClassroomHealth } from '../types'

interface Props {
  classrooms: ClassroomHealth[]
}

function ClassroomHealthCards({ classrooms }: Props) {
  if (classrooms.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma turma cadastrada na organização.</p>
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {classrooms.map((classroom) => {
        const deliveryRatePercent = Math.round(classroom.deliveryRate * 100)
        return (
          <div key={classroom.classroomId} className="rounded-lg border p-4">
            <p className="font-medium">{classroom.classroomName}</p>
            <p className="text-xs text-muted-foreground">{classroom.status}</p>
            <div className="mt-2 grid grid-cols-2 gap-2">
              <div>
                <p className="text-xs text-muted-foreground">Taxa de entrega</p>
                <p className="text-lg font-semibold">{deliveryRatePercent}%</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Média de notas</p>
                <p className="text-lg font-semibold">
                  {classroom.averageGrade === null ? 'Sem notas ainda' : classroom.averageGrade}
                </p>
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default ClassroomHealthCards
