import PendingEvaluationsBadge from './PendingEvaluationsBadge'
import LastTaskGradeChart from './LastTaskGradeChart'
import StudentsWithoutSubmissionList from './StudentsWithoutSubmissionList'
import StudentAverageGradesList from './StudentAverageGradesList'
import { useProfessorDashboard } from '../hooks/useProfessorDashboard'

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

      <div>
        <h3 className="mb-2 text-sm font-medium">Distribuição de notas da última tarefa</h3>
        <LastTaskGradeChart grades={data.lastTaskGradeDistribution} />
      </div>

      <div>
        <h3 className="mb-2 text-sm font-medium">Alunos sem entrega na última tarefa</h3>
        <StudentsWithoutSubmissionList students={data.studentsWithoutSubmission} />
      </div>

      <div>
        <h3 className="mb-2 text-sm font-medium">Média de notas por aluno</h3>
        <StudentAverageGradesList students={data.averageGradePerStudent} />
      </div>
    </div>
  )
}

export default ProfessorDashboard
