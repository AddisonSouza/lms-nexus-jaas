import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useSwitchOrganization } from './useSwitchOrganization'
import * as session from '@lib/session'

vi.mock('@lib/session')

const mockSetToken = vi.fn()
const mockNavigate = vi.fn()

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ setToken: mockSetToken })),
}))
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })

function wrapper({ children }: { children: React.ReactNode }) {
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useSwitchOrganization', () => {
  it('stores the new token, clears the cache and lands on the root route', async () => {
    vi.mocked(session.switchOrganization).mockResolvedValue('token-of-org-2')
    const clear = vi.spyOn(queryClient, 'clear')

    const { result } = renderHook(() => useSwitchOrganization(), { wrapper })

    act(() => result.current.mutate('org-2'))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    expect(session.switchOrganization).toHaveBeenCalledWith('org-2')
    expect(mockSetToken).toHaveBeenCalledWith('token-of-org-2')
    expect(clear).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true })
  })

  it('keeps the session untouched when the switch is refused', async () => {
    vi.mocked(session.switchOrganization).mockRejectedValue({ response: { status: 403 } })

    const { result } = renderHook(() => useSwitchOrganization(), { wrapper })
    act(() => result.current.mutate('org-not-mine'))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(mockSetToken).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
