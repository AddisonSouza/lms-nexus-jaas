import { useState } from 'react'
import { Download, Loader2 } from 'lucide-react'
import PeriodSelector from './PeriodSelector'
import MetricsCards from './MetricsCards'
import DashboardCharts from './DashboardCharts'
import ActivityFeed from './ActivityFeed'
import { useAdminDashboard } from '../hooks/useAdminDashboard'
import { exportAdminDashboardPdf } from '../api/dashboard'
import type { DashboardPeriod } from '../types'

interface Props {
  organizationId: string
}

function defaultPeriod(): DashboardPeriod {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - 30)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

function AdminDashboard({ organizationId }: Props) {
  const [period, setPeriod] = useState<DashboardPeriod>(defaultPeriod)
  const [isExporting, setIsExporting] = useState(false)
  const { data, isLoading, isError } = useAdminDashboard(organizationId, period)

  async function handleExportPdf() {
    setIsExporting(true)
    try {
      const blob = await exportAdminDashboardPdf(organizationId, period)
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <PeriodSelector value={period} onChange={setPeriod} />
        <button
          onClick={handleExportPdf}
          disabled={isExporting || !data}
          className="flex items-center gap-2 rounded border px-3 py-1.5 text-sm hover:bg-muted disabled:opacity-50"
        >
          {isExporting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
          Exportar PDF
        </button>
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando dashboard...</p>}
      {isError && <p className="text-sm text-destructive">Não foi possível carregar o dashboard.</p>}

      {data && (
        <>
          <MetricsCards dashboard={data} />
          <DashboardCharts dashboard={data} />
          <div>
            <h2 className="mb-2 text-sm font-medium">Últimas atividades</h2>
            <ActivityFeed activity={data.activity} />
          </div>
        </>
      )}
    </div>
  )
}

export default AdminDashboard
