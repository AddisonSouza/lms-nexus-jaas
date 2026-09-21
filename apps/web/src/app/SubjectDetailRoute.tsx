import { useParams } from 'react-router-dom'
import ProfessorDashboard from '@features/dashboard/components/ProfessorDashboard'
import SubjectDetailPage from '@features/curriculum/components/SubjectDetailPage'

/**
 * O slot é montado aqui porque `ProfessorDashboard` vive noutra feature, mas quem
 * decide exibi-lo é a página: ela tem a disciplina e sabe quem a leciona. O papel
 * na organização era largo demais e deixava gestor e admin com um bloco vazio.
 */
function SubjectDetailRoute() {
  const { subjectId } = useParams<{ subjectId: string }>()

  return (
    <SubjectDetailPage
      dashboardSlot={subjectId ? <ProfessorDashboard subjectId={subjectId} /> : null}
    />
  )
}

export default SubjectDetailRoute
