import { render, screen } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import RequireOrganization from './RequireOrganization'

let mockOrganizationId: string | null = null

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ organizationId: mockOrganizationId })),
}))

beforeEach(() => {
  mockOrganizationId = null
})

function renderRoute() {
  return render(
    <MemoryRouter initialEntries={['/classrooms']}>
      <Routes>
        <Route element={<RequireOrganization />}>
          <Route path="/classrooms" element={<div>classroom list</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}

describe('RequireOrganization', () => {
  it('renders the empty state instead of the page when the user has no organization', () => {
    mockOrganizationId = null
    renderRoute()

    expect(screen.getByText('Você não faz parte de nenhuma organização')).toBeTruthy()
    expect(screen.queryByText('classroom list')).toBeNull()
  })

  it('offers both paths in the empty state', () => {
    mockOrganizationId = null
    renderRoute()

    expect(screen.getByText('Criar organização')).toBeTruthy()
    expect(screen.getByText('Entrar por convite')).toBeTruthy()
  })

  it('renders the page when the user belongs to an organization', () => {
    mockOrganizationId = 'org-1'
    renderRoute()

    expect(screen.getByText('classroom list')).toBeTruthy()
    expect(screen.queryByText('Você não faz parte de nenhuma organização')).toBeNull()
  })
})
