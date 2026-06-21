import { NavLink } from 'react-router-dom'
import { BookOpen, BookOpenCheck, ClipboardList, LayoutDashboard } from 'lucide-react'
import { useAuthStore } from '@store/authStore'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors ${
    isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
  }`

function Sidebar() {
  const role = useAuthStore((s) => s.role)
  const organizationId = useAuthStore((s) => s.organizationId)

  return (
    <aside className="w-56 shrink-0 border-r bg-background">
      <nav className="flex flex-col gap-1 p-3">
        {organizationId && (
          <NavLink to={`/organizations/${organizationId}`} className={linkClass} end>
            <LayoutDashboard className="h-4 w-4" />
            Painel
          </NavLink>
        )}

        <NavLink to="/classrooms" className={linkClass}>
          <BookOpen className="h-4 w-4" />
          Turmas
        </NavLink>

        {(role === 'PROFESSOR' || role === 'ADMIN_ORG' || role === 'GESTOR') && (
          <NavLink to="/curriculum" className={linkClass}>
            <BookOpenCheck className="h-4 w-4" />
            Disciplinas
          </NavLink>
        )}

        {role === 'PROFESSOR' && (
          <NavLink to="/assessment/tasks" className={linkClass}>
            <ClipboardList className="h-4 w-4" />
            Tarefas
          </NavLink>
        )}

        {role === 'ALUNO' && (
          <NavLink to="/assessment/student-tasks" className={linkClass}>
            <ClipboardList className="h-4 w-4" />
            Minhas Tarefas
          </NavLink>
        )}

      </nav>
    </aside>
  )
}

export default Sidebar
