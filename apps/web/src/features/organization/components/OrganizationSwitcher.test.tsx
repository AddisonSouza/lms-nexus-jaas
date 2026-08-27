import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import OrganizationSwitcher from './OrganizationSwitcher'
import * as orgApi from '../api/organization-api'

vi.mock('../api/organization-api')

const mockMutate = vi.fn()
vi.mock('../hooks/useSwitchOrganization', () => ({
  useSwitchOrganization: () => ({ mutate: mockMutate, isPending: false }),
}))

let organizationId: string | null = 'org-1'
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ organizationId })),
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  organizationId = 'org-1'
  vi.mocked(orgApi.listOrganizations).mockResolvedValue([
    { id: 'org-1', name: 'Escola Alfa', role: 'ADMIN_ORG' },
    { id: 'org-2', name: 'Escola Beta', role: 'ALUNO' },
  ])
})

describe('OrganizationSwitcher', () => {
  it('shows the active organization and the neutral label of the role', async () => {
    render(<OrganizationSwitcher />, { wrapper })

    expect(await screen.findByText('Escola Alfa')).toBeTruthy()
    expect(screen.getByText('Administrador')).toBeTruthy()
  })

  it('lists the organizations with the active one marked and the others tagged', async () => {
    render(<OrganizationSwitcher />, { wrapper })
    await screen.findByText('Escola Alfa')

    await userEvent.click(screen.getByRole('button', { name: /trocar de organização/i }))

    expect(await screen.findByText('Suas organizações')).toBeTruthy()
    expect(screen.getByLabelText('Organização ativa')).toBeTruthy()
    expect(screen.getByText('Aluno')).toBeTruthy()
  })

  it('switches to another organization when it is picked', async () => {
    render(<OrganizationSwitcher />, { wrapper })
    await screen.findByText('Escola Alfa')

    await userEvent.click(screen.getByRole('button', { name: /trocar de organização/i }))
    await userEvent.click(await screen.findByRole('button', { name: /escola beta/i }))

    await waitFor(() => expect(mockMutate).toHaveBeenCalledWith('org-2'))
  })

  it('does not switch when the active organization is picked again', async () => {
    render(<OrganizationSwitcher />, { wrapper })
    await screen.findByText('Escola Alfa')

    await userEvent.click(screen.getByRole('button', { name: /trocar de organização/i }))
    await userEvent.click(await screen.findByRole('button', { name: /escola alfa/i }))

    expect(mockMutate).not.toHaveBeenCalled()
  })

  it('offers creating an organization even with no organization at all', async () => {
    organizationId = null
    vi.mocked(orgApi.listOrganizations).mockResolvedValue([])

    render(<OrganizationSwitcher />, { wrapper })

    expect(await screen.findByText('Sem organização')).toBeTruthy()
    await userEvent.click(screen.getByRole('button', { name: /trocar de organização/i }))

    expect(await screen.findByText(/ainda não pertence a nenhuma organização/i)).toBeTruthy()
    expect(screen.getByRole('link', { name: /criar organização/i }).getAttribute('href'))
      .toBe('/organizations/new')
  })

  it('does not claim there is no organization while the list is loading', async () => {
    let resolveList: (orgs: orgApi.UserOrganization[]) => void = () => {}
    vi.mocked(orgApi.listOrganizations).mockReturnValue(
      new Promise((resolve) => {
        resolveList = resolve
      }),
    )

    render(<OrganizationSwitcher />, { wrapper })

    expect(screen.queryByText('Sem organização')).toBeNull()
    expect(screen.getByText('Carregando...')).toBeTruthy()

    resolveList([{ id: 'org-1', name: 'Escola Alfa', role: 'ADMIN_ORG' }])
    expect(await screen.findByText('Escola Alfa')).toBeTruthy()
  })

  it('carries the full name in a tooltip, since long names are truncated', async () => {
    const longName = 'Escola Municipal Professora Maria das Dores de Albuquerque'
    vi.mocked(orgApi.listOrganizations).mockResolvedValue([
      { id: 'org-1', name: longName, role: 'ADMIN_ORG' },
    ])

    render(<OrganizationSwitcher />, { wrapper })

    expect((await screen.findByText(longName)).getAttribute('title')).toBe(longName)

    await userEvent.click(screen.getByRole('button', { name: /trocar de organização/i }))

    const inTheList = (await screen.findAllByText(longName)).filter(
      (element) => element.getAttribute('title') === longName,
    )
    expect(inTheList.length).toBe(2)
  })
})
