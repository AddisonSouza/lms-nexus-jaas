import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateOrganization } from './useCreateOrganization'
import * as orgApi from '../api/organization-api'
import * as session from '@lib/session'

vi.mock('../api/organization-api')
vi.mock('@lib/session')

const mockSetToken = vi.fn()
const mockNavigate = vi.fn()

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ setToken: mockSetToken }),
  ),
}))
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })

function wrapper({ children }: { children: React.ReactNode }) {
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateOrganization', () => {
  it('creates org, switches organization, updates store and navigates', async () => {
    vi.mocked(orgApi.createOrganization).mockResolvedValue({
      id: 'org-42', name: 'My Org', description: null, ownerId: 'u1', createdAt: '',
    })
    vi.mocked(session.switchOrganization).mockResolvedValue('fresh-token')

    const { result } = renderHook(() => useCreateOrganization(), { wrapper })

    act(() => result.current.mutate({ name: 'My Org' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    expect(session.switchOrganization).toHaveBeenCalledWith('org-42')
    expect(mockSetToken).toHaveBeenCalledWith('fresh-token')
    expect(mockNavigate).toHaveBeenCalledWith('/organizations/org-42')
  })

  it('exposes error on 409 conflict', async () => {
    vi.mocked(orgApi.createOrganization).mockRejectedValue({ response: { status: 409 } })

    const { result } = renderHook(() => useCreateOrganization(), { wrapper })
    act(() => result.current.mutate({ name: 'Dup' }))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(409)
  })

  it('clears the cache so the organization list is refetched with the new one', async () => {
    vi.mocked(orgApi.createOrganization).mockResolvedValue({
      id: 'org-99', name: 'Nova', description: null, ownerId: 'u1', createdAt: '',
    })
    vi.mocked(session.switchOrganization).mockResolvedValue('token-of-org-99')
    const clear = vi.spyOn(queryClient, 'clear')

    const { result } = renderHook(() => useCreateOrganization(), { wrapper })
    act(() => result.current.mutate({ name: 'Nova' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(clear).toHaveBeenCalled()
  })
})
