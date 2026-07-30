import { render, screen } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import RootRedirect from './RootRedirect'

let mockOrganizationId: string | null = null

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ organizationId: mockOrganizationId })),
}))

beforeEach(() => {
  mockOrganizationId = null
})

function renderRoute() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route path="/" element={<RootRedirect />} />
        <Route path="/welcome" element={<div>welcome screen</div>} />
        <Route path="/classrooms" element={<div>classroom list</div>} />
        <Route path="/organizations/new" element={<div>create organization form</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('RootRedirect', () => {
  it('sends a user without an organization to the welcome screen', () => {
    mockOrganizationId = null
    renderRoute()

    expect(screen.getByText('welcome screen')).toBeTruthy()
  })

  it('no longer forces the organization creation form', () => {
    mockOrganizationId = null
    renderRoute()

    expect(screen.queryByText('create organization form')).toBeNull()
  })

  it('sends a user with an organization to the classroom list', () => {
    mockOrganizationId = 'org-1'
    renderRoute()

    expect(screen.getByText('classroom list')).toBeTruthy()
  })
})
