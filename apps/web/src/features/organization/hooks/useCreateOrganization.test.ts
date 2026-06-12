import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateOrganization } from './useCreateOrganization'
import * as orgApi from '../api/organization-api'
import * as authApi from '@features/auth/api/auth-api'

vi.mock('../api/organization-api')
vi.mock('@features/auth/api/auth-api')

const mockSetOrganization = vi.fn()
const mockNavigate = vi.fn()

vi.mock('@features/auth/store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ setOrganization: mockSetOrganization }),
  ),
}))
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateOrganization', () => {
  it('creates org, calls refresh with orgId, updates store and navigates', async () => {
    vi.mocked(orgApi.createOrganization).mockResolvedValue({
      id: 'org-42', name: 'My Org', description: null, ownerId: 'u1', createdAt: '',
    })
    vi.mocked(authApi.refreshTokens).mockResolvedValue({ accessToken: 'fresh-token' })

    const { result } = renderHook(() => useCreateOrganization(), { wrapper })

    act(() => result.current.mutate({ name: 'My Org' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    expect(authApi.refreshTokens).toHaveBeenCalledWith('org-42')
    expect(mockSetOrganization).toHaveBeenCalledWith('fresh-token', 'org-42')
    expect(mockNavigate).toHaveBeenCalledWith('/organizations/org-42')
  })

  it('exposes error on 409 conflict', async () => {
    vi.mocked(orgApi.createOrganization).mockRejectedValue({ response: { status: 409 } })

    const { result } = renderHook(() => useCreateOrganization(), { wrapper })
    act(() => result.current.mutate({ name: 'Dup' }))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(409)
  })
})
