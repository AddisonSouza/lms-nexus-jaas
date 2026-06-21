import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import OrganizationRoute from './OrganizationRoute'

let mockRole: string | null = 'ADMIN_ORG'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole })),
}))

vi.mock('@features/dashboard/components/AdminDashboard', () => ({
  default: ({ organizationId }: { organizationId: string }) => (
    <div data-testid="admin-dashboard">AdminDashboard for {organizationId}</div>
  ),
}))

vi.mock('@features/dashboard/components/GestorDashboard', () => ({
  default: ({ organizationId }: { organizationId: string }) => (
    <div data-testid="gestor-dashboard">GestorDashboard for {organizationId}</div>
  ),
}))

vi.mock('@features/dashboard/components/StudentDashboard', () => ({
  default: () => <div data-testid="student-dashboard">StudentDashboard</div>,
}))

beforeEach(() => {
  vi.clearAllMocks()
  mockRole = 'ADMIN_ORG'
})

function renderRoute(organizationId = 'org-1') {
  return render(
    <MemoryRouter initialEntries={[`/organizations/${organizationId}`]}>
      <Routes>
        <Route path="/organizations/:id" element={<OrganizationRoute />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('OrganizationRoute', () => {
  it('renders the AdminDashboard for ADMIN_ORG users', () => {
    mockRole = 'ADMIN_ORG'
    renderRoute('org-1')

    expect(screen.getByTestId('admin-dashboard')).toBeTruthy()
    expect(screen.getByText('AdminDashboard for org-1')).toBeTruthy()
  })

  it('renders the GestorDashboard for GESTOR users', () => {
    mockRole = 'GESTOR'
    renderRoute('org-1')

    expect(screen.getByTestId('gestor-dashboard')).toBeTruthy()
    expect(screen.getByText('GestorDashboard for org-1')).toBeTruthy()
  })

  it('renders the StudentDashboard for ALUNO users', () => {
    mockRole = 'ALUNO'
    renderRoute('org-1')

    expect(screen.getByTestId('student-dashboard')).toBeTruthy()
  })

  it('renders the generic organization page for non-ADMIN_ORG/GESTOR/ALUNO roles', () => {
    mockRole = 'PROFESSOR'
    renderRoute('org-1')

    expect(screen.queryByTestId('admin-dashboard')).toBeNull()
    expect(screen.queryByTestId('gestor-dashboard')).toBeNull()
    expect(screen.queryByTestId('student-dashboard')).toBeNull()
    expect(screen.getByText('Turmas')).toBeTruthy()
  })
})
