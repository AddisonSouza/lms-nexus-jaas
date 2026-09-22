import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement, type ReactNode } from 'react'
import AddMemberDialog from './AddMemberDialog'
import * as orgMemberApi from '../api/org-member-api'
import { useAuthStore } from '@store/authStore'

vi.mock('../api/org-member-api')

// O jsdom não traz nada disso, e o Positioner do combobox usa os três.
beforeAll(() => {
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    }
  )
  Element.prototype.scrollIntoView = vi.fn()
  if (!Element.prototype.hasPointerCapture) {
    Element.prototype.hasPointerCapture = () => false
  }
})

const ANA = { id: 'm-1', userId: 'user-1', name: 'Ana Silva', email: 'ana@test.com', role: 'PROFESSOR' as const }
const BRUNO = { id: 'm-2', userId: 'user-2', name: 'Bruno Lima', email: 'bruno@test.com', role: 'ALUNO' as const }

const onSubmit = vi.fn()
const onClose = vi.fn()

function wrapper({ children }: { children: ReactNode }) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

function renderDialog({ isPending = false, existingUserIds = [] as string[] } = {}) {
  return render(
    <AddMemberDialog
      open
      onClose={onClose}
      onSubmit={onSubmit}
      isPending={isPending}
      existingUserIds={existingUserIds}
    />,
    { wrapper }
  )
}

/** A lista só monta depois que o campo é clicado — é assim que o usuário a abre. */
async function openList() {
  await userEvent.click(screen.getByLabelText('Pessoa *'))
}

beforeEach(() => {
  vi.clearAllMocks()
  useAuthStore.setState({ organizationId: 'org-1' })
  vi.mocked(orgMemberApi.searchOrgMembers).mockResolvedValue({
    content: [ANA, BRUNO],
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 20,
  })
})

describe('AddMemberDialog', () => {
  it('lists the organization members the API returned, by name and email', async () => {
    renderDialog()
    await openList()

    expect(await screen.findByText('Ana Silva')).toBeTruthy()
    expect(screen.getByText('bruno@test.com')).toBeTruthy()
    // Ninguém precisa conhecer um UUID para achar a pessoa.
    expect(screen.queryByPlaceholderText('UUID do membro')).toBeNull()
  })

  it('asks the API to search instead of filtering on the client', async () => {
    renderDialog()
    await openList()
    await screen.findByText('Ana Silva')

    await userEvent.type(screen.getByLabelText('Pessoa *'), 'ana')

    await waitFor(() =>
      expect(orgMemberApi.searchOrgMembers).toHaveBeenCalledWith(
        'org-1',
        expect.objectContaining({ search: 'ana' })
      )
    )
  })

  it('marks who is already in the classroom instead of hiding them', async () => {
    renderDialog({ existingUserIds: ['user-1'] })
    await openList()

    expect(await screen.findByText('Ana Silva')).toBeTruthy()
    expect(screen.getByText(/já na turma/)).toBeTruthy()
  })

  it('submits the picked member and the chosen role', async () => {
    renderDialog()
    await openList()

    await userEvent.click(await screen.findByText('Bruno Lima'))
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ userId: 'user-2', role: 'ALUNO' })
  })

  it('does not submit without picking someone', async () => {
    renderDialog()

    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar' }))

    expect(await screen.findByText('Selecione um membro')).toBeTruthy()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('blocks a second submit while one is pending', () => {
    renderDialog({ isPending: true })

    expect((screen.getByRole('button', { name: 'Adicionar' }) as HTMLButtonElement).disabled).toBe(true)
  })
})
