import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Sidebar from './Sidebar'

let mockRole: string | null = 'ALUNO'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ role: mockRole, organizationId: 'org-1' }),
  ),
}))

vi.mock('@features/organization/components/OrganizationSwitcher', () => ({
  default: () => <div />,
}))

function renderSidebar(role: string | null) {
  mockRole = role
  return render(
    <MemoryRouter>
      <Sidebar />
    </MemoryRouter>,
  )
}

describe('Sidebar', () => {
  it('offers Disciplinas to the student, who needs the materials', () => {
    renderSidebar('ALUNO')

    expect(screen.getByRole('link', { name: 'Disciplinas' }).getAttribute('href')).toBe('/curriculum')
  })

  it('keeps Disciplinas for whoever teaches', () => {
    renderSidebar('PROFESSOR')

    expect(screen.getByRole('link', { name: 'Disciplinas' })).toBeTruthy()
  })

  it('does not offer the teacher task list to the student', () => {
    renderSidebar('ALUNO')

    expect(screen.queryByRole('link', { name: 'Tarefas' })).toBeNull()
    expect(screen.getByRole('link', { name: 'Minhas Tarefas' })).toBeTruthy()
  })

  it('shows Membros only to the organization admin', () => {
    renderSidebar('ALUNO')
    expect(screen.queryByRole('link', { name: 'Membros' })).toBeNull()

    renderSidebar('ADMIN_ORG')
    expect(screen.getByRole('link', { name: 'Membros' })).toBeTruthy()
  })
})
