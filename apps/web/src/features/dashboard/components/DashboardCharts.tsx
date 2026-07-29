import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { AdminDashboardData } from '../types'
import { Card, CardKicker } from '@components/ui/card'

interface Props {
  dashboard: AdminDashboardData
}

function DashboardCharts({ dashboard }: Props) {
  const membersData = Object.entries(dashboard.membersByRole).map(([role, count]) => ({ role, count }))
  const tasksData = [
    { label: 'Criadas', value: dashboard.tasksCreated },
    { label: 'Avaliadas', value: dashboard.tasksEvaluated },
  ]

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
      <Card elevation="sm">
        <CardKicker>Membros por papel</CardKicker>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={membersData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-divider)" />
            <XAxis dataKey="role" fontSize={12} stroke="var(--color-text)" />
            <YAxis allowDecimals={false} fontSize={12} stroke="var(--color-text)" />
            <Tooltip />
            <Bar dataKey="count" fill="var(--color-accent)" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Card>

      <Card elevation="sm">
        <CardKicker>Tarefas criadas vs. avaliadas</CardKicker>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={tasksData}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-divider)" />
            <XAxis dataKey="label" fontSize={12} stroke="var(--color-text)" />
            <YAxis allowDecimals={false} fontSize={12} stroke="var(--color-text)" />
            <Tooltip />
            <Bar dataKey="value" fill="var(--color-accent-2)" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  )
}

export default DashboardCharts
