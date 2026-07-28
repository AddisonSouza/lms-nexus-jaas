import type { AdminDashboardData } from '../types'
import { Card, CardKicker } from '@components/ui/card'

interface Props {
  dashboard: AdminDashboardData
}

function sumValues(record: Record<string, number>) {
  return Object.values(record).reduce((acc, value) => acc + value, 0)
}

function MetricsCards({ dashboard }: Props) {
  const activeClassrooms = dashboard.classroomsByStatus.ACTIVE ?? 0
  const archivedClassrooms = dashboard.classroomsByStatus.ARCHIVED ?? 0
  const totalMembers = sumValues(dashboard.membersByRole)
  const deliveryRatePercent = Math.round(dashboard.averageDeliveryRate * 100)

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <Card elevation="sm">
        <CardKicker>Turmas ativas / arquivadas</CardKicker>
        <p className="font-heading text-3xl leading-none">
          {activeClassrooms} / {archivedClassrooms}
        </p>
      </Card>

      <Card elevation="sm">
        <CardKicker>Membros ({totalMembers})</CardKicker>
        <p className="text-sm">
          {Object.entries(dashboard.membersByRole)
            .map(([role, count]) => `${role}: ${count}`)
            .join(' · ') || 'Sem membros no período'}
        </p>
      </Card>

      <Card elevation="sm">
        <CardKicker>Tarefas criadas / avaliadas</CardKicker>
        <p className="font-heading text-3xl leading-none">
          {dashboard.tasksCreated} / {dashboard.tasksEvaluated}
        </p>
      </Card>

      <Card
        elevation="sm"
        title="Média das taxas de entrega (submissões / alunos elegíveis) por tarefa criada no período"
      >
        <CardKicker>Taxa média de entrega</CardKicker>
        <p className="font-heading text-3xl leading-none">{deliveryRatePercent}%</p>
      </Card>
    </div>
  )
}

export default MetricsCards
