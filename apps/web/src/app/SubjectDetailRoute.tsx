import { useParams } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import ProfessorDashboard from '@features/dashboard/components/ProfessorDashboard'
import SubjectDetailPage from '@features/curriculum/components/SubjectDetailPage'

function SubjectDetailRoute() {
  const { subjectId } = useParams<{ subjectId: string }>()
  const role = useAuthStore((s) => s.role)

  return (
    <SubjectDetailPage
      dashboardSlot={role === 'PROFESSOR' && subjectId ? <ProfessorDashboard subjectId={subjectId} /> : null}
    />
  )
}

export default SubjectDetailRoute
