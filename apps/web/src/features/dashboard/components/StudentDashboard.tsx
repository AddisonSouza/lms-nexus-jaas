import UpcomingTasksList from './UpcomingTasksList'
import SubmissionStatusSummary from './SubmissionStatusSummary'
import RecentGradesList from './RecentGradesList'
import SubjectAverageGradesList from './SubjectAverageGradesList'
import { useStudentDashboard } from '../hooks/useStudentDashboard'

function StudentDashboard() {
  const { data, isLoading, isError } = useStudentDashboard()

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Carregando dashboard...</p>
  }
  if (isError || !data) {
    return <p className="text-sm text-destructive">Não foi possível carregar o dashboard.</p>
  }

  return (
    <div className="space-y-6">
      <SubmissionStatusSummary submittedCount={data.submittedTasksCount} pendingCount={data.pendingTasksCount} />

      <div>
        <h3 className="mb-2 text-sm font-medium">Próximas tarefas</h3>
        <UpcomingTasksList tasks={data.upcomingPendingTasks} />
      </div>

      <div>
        <h3 className="mb-2 text-sm font-medium">Últimas notas e feedbacks</h3>
        <RecentGradesList grades={data.recentGrades} />
      </div>

      <div>
        <h3 className="mb-2 text-sm font-medium">Média por disciplina</h3>
        <SubjectAverageGradesList subjects={data.averageGradePerSubject} />
      </div>
    </div>
  )
}

export default StudentDashboard
