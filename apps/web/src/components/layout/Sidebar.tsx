import { NavLink } from 'react-router-dom'
import { BookOpen, BookOpenCheck, ClipboardList, LayoutDashboard } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { CardKicker } from '@components/ui/card'
import OrganizationSwitcher from '@features/organization/components/OrganizationSwitcher'
import { cn } from '@features/lib/utils'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    'flex items-center gap-2.5 rounded-full px-3 py-2 text-sm font-medium transition-colors',
    isActive ? 'bg-accent text-accent-foreground' : 'text-foreground hover:bg-muted',
  )

function Sidebar() {
  const role = useAuthStore((s) => s.role)
  const organizationId = useAuthStore((s) => s.organizationId)

  return (
    <aside className="w-56 shrink-0 p-4">
      <div className="mb-3">
        <OrganizationSwitcher />
      </div>

      <nav className="flex flex-col gap-1">
        <CardKicker className="px-3 pb-1">Ensino</CardKicker>

        {organizationId && (
          <NavLink to={`/organizations/${organizationId}`} className={linkClass} end>
            <LayoutDashboard className="h-[19px] w-[19px] shrink-0" />
            Painel
          </NavLink>
        )}

        <NavLink to="/classrooms" className={linkClass}>
          <BookOpen className="h-[19px] w-[19px] shrink-0" />
          Turmas
        </NavLink>

        {(role === 'PROFESSOR' || role === 'ADMIN_ORG' || role === 'GESTOR') && (
          <NavLink to="/curriculum" className={linkClass}>
            <BookOpenCheck className="h-[19px] w-[19px] shrink-0" />
            Disciplinas
          </NavLink>
        )}

        {role === 'PROFESSOR' && (
          <NavLink to="/assessment/tasks" className={linkClass}>
            <ClipboardList className="h-[19px] w-[19px] shrink-0" />
            Tarefas
          </NavLink>
        )}

        {role === 'ALUNO' && (
          <NavLink to="/assessment/student-tasks" className={linkClass}>
            <ClipboardList className="h-[19px] w-[19px] shrink-0" />
            Minhas Tarefas
          </NavLink>
        )}
      </nav>
    </aside>
  )
}

export default Sidebar
