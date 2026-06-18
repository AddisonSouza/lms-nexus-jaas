import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import OrganizationDashboardPage from './OrganizationDashboardPage'

let mockRole: string | null = 'ADMIN_ORG'

vi.mock('@features/auth/store/authStore', () => ({
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

function renderPage(organizationId = 'org-1') {
  return render(
    <MemoryRouter initialEntries={[`/organizations/${organizationId}`]}>
      <Routes>
        <Route path="/organizations/:id" element={<OrganizationDashboardPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('OrganizationDashboardPage', () => {
  it('renders the AdminDashboard for ADMIN_ORG users', () => {
    mockRole = 'ADMIN_ORG'
    renderPage('org-1')

    expect(screen.getByTestId('admin-dashboard')).toBeTruthy()
    expect(screen.getByText('AdminDashboard for org-1')).toBeTruthy()
  })

  it('renders the GestorDashboard for GESTOR users', () => {
    mockRole = 'GESTOR'
    renderPage('org-1')

    expect(screen.getByTestId('gestor-dashboard')).toBeTruthy()
    expect(screen.getByText('GestorDashboard for org-1')).toBeTruthy()
  })

  it('renders the StudentDashboard for ALUNO users', () => {
    mockRole = 'ALUNO'
    renderPage('org-1')

    expect(screen.getByTestId('student-dashboard')).toBeTruthy()
  })

  it('renders the generic organization page for non-ADMIN_ORG/GESTOR/ALUNO roles', () => {
    mockRole = 'PROFESSOR'
    renderPage('org-1')

    expect(screen.queryByTestId('admin-dashboard')).toBeNull()
    expect(screen.queryByTestId('gestor-dashboard')).toBeNull()
    expect(screen.queryByTestId('student-dashboard')).toBeNull()
    expect(screen.getByText('Turmas')).toBeTruthy()
  })
})
