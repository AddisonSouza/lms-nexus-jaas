import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { AdminDashboardData } from '../types'

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
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div className="rounded-lg border p-4">
        <p className="mb-2 text-sm font-medium">Membros por papel</p>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={membersData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="role" fontSize={12} />
            <YAxis allowDecimals={false} fontSize={12} />
            <Tooltip />
            <Bar dataKey="count" fill="#2563eb" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="rounded-lg border p-4">
        <p className="mb-2 text-sm font-medium">Tarefas criadas vs. avaliadas</p>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={tasksData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="label" fontSize={12} />
            <YAxis allowDecimals={false} fontSize={12} />
            <Tooltip />
            <Bar dataKey="value" fill="#16a34a" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

export default DashboardCharts
