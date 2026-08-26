import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Check, ChevronsUpDown, Plus } from 'lucide-react'
import { Popover, PopoverTrigger, PopoverContent } from '@components/ui/popover'
import { Badge } from '@components/ui/badge'
import { CardKicker } from '@components/ui/card'
import { useAuthStore } from '@store/authStore'
import { useOrganizations } from '../hooks/useOrganizations'
import { useSwitchOrganization } from '../hooks/useSwitchOrganization'
import type { UserOrganization } from '../api/organization-api'

// Neutral labels: the user's gender is unknown, so no gendered forms.
const roleLabels: Record<UserOrganization['role'], string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
}

function initials(name: string) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((word) => word[0])
    .join('')
    .toUpperCase()
}

function OrganizationSwitcher() {
  const [open, setOpen] = useState(false)
  const organizationId = useAuthStore((s) => s.organizationId)
  const { data: organizations = [] } = useOrganizations()
  const switchOrganization = useSwitchOrganization()

  const active = organizations.find((org) => org.id === organizationId)

  function handleSelect(org: UserOrganization) {
    setOpen(false)
    if (org.id !== organizationId) {
      switchOrganization.mutate(org.id)
    }
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        className="flex w-full items-center gap-2.5 rounded-full px-2 py-2 text-left transition-colors hover:bg-muted disabled:opacity-60"
        disabled={switchOrganization.isPending}
        aria-label="Trocar de organização"
      >
        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-accent font-heading text-xs text-accent-foreground">
          {active ? initials(active.name) : '—'}
        </span>
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-medium">
            {active ? active.name : 'Sem organização'}
          </span>
          <span className="block truncate text-xs text-muted-foreground">
            {active ? roleLabels[active.role] : 'Nenhuma selecionada'}
          </span>
        </span>
        <ChevronsUpDown className="h-4 w-4 shrink-0 text-muted-foreground" />
      </PopoverTrigger>

      <PopoverContent align="start" className="w-64 p-0">
        <div className="flex flex-col gap-[var(--space-2)] p-[var(--space-2)]">
          <CardKicker className="px-2 pt-1">Suas organizações</CardKicker>

          {organizations.length === 0 && (
            <p className="px-2 py-2 text-sm text-muted-foreground">
              Você ainda não pertence a nenhuma organização.
            </p>
          )}

          {organizations.length > 0 && (
            <ul className="flex flex-col gap-0.5">
              {organizations.map((org) => {
                const isActive = org.id === organizationId
                return (
                  <li key={org.id}>
                    <button
                      onClick={() => handleSelect(org)}
                      aria-current={isActive ? 'true' : undefined}
                      className="flex w-full items-center gap-2.5 rounded-[var(--radius-md)] px-2 py-2 text-left hover:bg-muted"
                    >
                      <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-accent font-heading text-[11px] text-accent-foreground">
                        {initials(org.name)}
                      </span>
                      <span className="min-w-0 flex-1 truncate text-sm">{org.name}</span>
                      {isActive ? (
                        <Check className="h-4 w-4 shrink-0 text-accent" aria-label="Organização ativa" />
                      ) : (
                        <Badge>{roleLabels[org.role]}</Badge>
                      )}
                    </button>
                  </li>
                )
              })}
            </ul>
          )}

          <div className="border-t border-border pt-[var(--space-2)]">
            <Link
              to="/organizations/new"
              onClick={() => setOpen(false)}
              className="flex w-full items-center gap-2.5 rounded-[var(--radius-md)] px-2 py-2 text-sm hover:bg-muted"
            >
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full border border-border">
                <Plus className="h-4 w-4" />
              </span>
              Criar organização
            </Link>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}

export default OrganizationSwitcher
