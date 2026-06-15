import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateTopic } from './useCreateTopic'
import * as topicApi from '../api/topic-api'

vi.mock('../api/topic-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateTopic', () => {
  it('calls createTopic and resolves on success', async () => {
    vi.mocked(topicApi.createTopic).mockResolvedValue({
      id: 'tp-1',
      subjectId: 's1',
      organizationId: 'o1',
      title: 'Introdução',
      position: 0,
      createdAt: '',
      updatedAt: null,
    })

    const { result } = renderHook(() => useCreateTopic('s1'), { wrapper })
    act(() => result.current.mutate('Introdução'))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(topicApi.createTopic).toHaveBeenCalledWith('s1', 'Introdução')
  })

  it('exposes error on failure', async () => {
    vi.mocked(topicApi.createTopic).mockRejectedValue({ response: { status: 404 } })

    const { result } = renderHook(() => useCreateTopic('s1'), { wrapper })
    act(() => result.current.mutate('Tópico inválido'))

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
