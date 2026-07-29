import type { ClassroomHealth } from '../types'
import { Card, CardKicker } from '@components/ui/card'
import { Badge } from '@components/ui/badge'

interface Props {
  classrooms: ClassroomHealth[]
}

function statusBadgeVariant(deliveryRate: number): 'accent-2' | 'neutral' | 'accent' {
  if (deliveryRate >= 0.75) return 'accent-2'
  if (deliveryRate >= 0.5) return 'neutral'
  return 'accent'
}

function ClassroomHealthCards({ classrooms }: Props) {
  if (classrooms.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma turma cadastrada na organização.</p>
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {classrooms.map((classroom) => {
        const deliveryRatePercent = Math.round(classroom.deliveryRate * 100)
        return (
          <Card key={classroom.classroomId} elevation="sm">
            <div className="flex items-start justify-between gap-2">
              <span className="font-heading text-[17px] leading-tight">{classroom.classroomName}</span>
              <Badge variant={statusBadgeVariant(classroom.deliveryRate)}>{classroom.status}</Badge>
            </div>
            <div className="mt-2 h-2 overflow-hidden rounded-full bg-neutral-200">
              <div
                className="h-full rounded-full bg-accent-2"
                style={{ width: `${deliveryRatePercent}%` }}
              />
            </div>
            <div className="mt-2 grid grid-cols-2 gap-2">
              <div>
                <CardKicker>Taxa de entrega</CardKicker>
                <p className="font-heading text-lg">{deliveryRatePercent}%</p>
              </div>
              <div>
                <CardKicker>Média de notas</CardKicker>
                <p className="font-heading text-lg">
                  {classroom.averageGrade === null ? 'Sem notas ainda' : classroom.averageGrade}
                </p>
              </div>
            </div>
          </Card>
        )
      })}
    </div>
  )
}

export default ClassroomHealthCards
