import PendingEvaluationsBadge from './PendingEvaluationsBadge'
import LastTaskGradeChart from './LastTaskGradeChart'
import StudentsWithoutSubmissionList from './StudentsWithoutSubmissionList'
import StudentAverageGradesList from './StudentAverageGradesList'
import { useProfessorDashboard } from '../hooks/useProfessorDashboard'
import { Card, CardKicker } from '@components/ui/card'

interface Props {
  subjectId: string
}

function ProfessorDashboard({ subjectId }: Props) {
  const { data, isLoading, isError } = useProfessorDashboard(subjectId)

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Carregando dashboard...</p>
  }
  if (isError || !data) {
    return <p className="text-sm text-destructive">Não foi possível carregar o dashboard.</p>
  }

  return (
    <div className="space-y-6">
      <PendingEvaluationsBadge count={data.pendingEvaluationsCount} />

      <Card elevation="sm">
        <CardKicker>Distribuição de notas</CardKicker>
        <h4 className="mt-0.5">Última tarefa</h4>
        <LastTaskGradeChart grades={data.lastTaskGradeDistribution} />
      </Card>

      <div>
        <h4 className="mb-2">Alunos sem entrega na última tarefa</h4>
        <StudentsWithoutSubmissionList students={data.studentsWithoutSubmission} />
      </div>

      <div>
        <h4 className="mb-2">Média de notas por aluno</h4>
        <StudentAverageGradesList students={data.averageGradePerStudent} />
      </div>
    </div>
  )
}

export default ProfessorDashboard
