import type { AdminDashboardData } from '../types'

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
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
      <div className="rounded-lg border p-4">
        <p className="text-xs text-muted-foreground">Turmas ativas / arquivadas</p>
        <p className="text-2xl font-semibold">
          {activeClassrooms} / {archivedClassrooms}
        </p>
      </div>

      <div className="rounded-lg border p-4">
        <p className="text-xs text-muted-foreground">Membros ({totalMembers})</p>
        <p className="text-sm">
          {Object.entries(dashboard.membersByRole)
            .map(([role, count]) => `${role}: ${count}`)
            .join(' · ') || 'Sem membros no período'}
        </p>
      </div>

      <div className="rounded-lg border p-4">
        <p className="text-xs text-muted-foreground">Tarefas criadas / avaliadas</p>
        <p className="text-2xl font-semibold">
          {dashboard.tasksCreated} / {dashboard.tasksEvaluated}
        </p>
      </div>

      <div className="rounded-lg border p-4" title="Média das taxas de entrega (submissões / alunos elegíveis) por tarefa criada no período">
        <p className="text-xs text-muted-foreground">Taxa média de entrega</p>
        <p className="text-2xl font-semibold">{deliveryRatePercent}%</p>
      </div>
    </div>
  )
}

export default MetricsCards
