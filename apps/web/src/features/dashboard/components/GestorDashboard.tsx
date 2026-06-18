import { useState } from 'react'
import { Download, Loader2 } from 'lucide-react'
import ClassroomHealthCards from './ClassroomHealthCards'
import AtRiskStudentsList from './AtRiskStudentsList'
import { useGestorDashboard } from '../hooks/useGestorDashboard'
import { exportGestorDashboardPdf } from '../api/gestor-dashboard'

interface Props {
  organizationId: string
}

function GestorDashboard({ organizationId }: Props) {
  const [isExporting, setIsExporting] = useState(false)
  const { data, isLoading, isError } = useGestorDashboard(organizationId)

  async function handleExportPdf() {
    setIsExporting(true)
    try {
      const blob = await exportGestorDashboardPdf(organizationId)
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-end gap-4">
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
          <div>
            <h2 className="mb-2 text-sm font-medium">Saúde das turmas</h2>
            <ClassroomHealthCards classrooms={data.classrooms} />
          </div>
          <div>
            <h2 className="mb-2 text-sm font-medium">Alunos em risco</h2>
            <AtRiskStudentsList classrooms={data.classrooms} />
          </div>
        </>
      )}
    </div>
  )
}

export default GestorDashboard
