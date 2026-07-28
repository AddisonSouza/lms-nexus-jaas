import { useState } from 'react'
import { Download, Loader2 } from 'lucide-react'
import ClassroomHealthCards from './ClassroomHealthCards'
import AtRiskStudentsList from './AtRiskStudentsList'
import { useGestorDashboard } from '../hooks/useGestorDashboard'
import { exportGestorDashboardPdf } from '../api/gestor-dashboard'
import { Button } from '@components/ui/button'

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
        <Button variant="secondary" onClick={handleExportPdf} disabled={isExporting || !data}>
          {isExporting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
          Exportar PDF
        </Button>
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando dashboard...</p>}
      {isError && <p className="text-sm text-destructive">Não foi possível carregar o dashboard.</p>}

      {data && (
        <>
          <div>
            <h4 className="mb-2">Saúde das turmas</h4>
            <ClassroomHealthCards classrooms={data.classrooms} />
          </div>
          <div>
            <h4 className="mb-2">Alunos em risco</h4>
            <AtRiskStudentsList classrooms={data.classrooms} />
          </div>
        </>
      )}
    </div>
  )
}

export default GestorDashboard
