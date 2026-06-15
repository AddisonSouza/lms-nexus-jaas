import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateContent } from './useCreateContent'
import * as contentApi from '../api/content-api'

vi.mock('../api/content-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateContent', () => {
  it('calls createContent with payload and resolves on success', async () => {
    vi.mocked(contentApi.createContent).mockResolvedValue({
      id: 'c1',
      topicId: 't1',
      organizationId: 'o1',
      title: 'Aula 1',
      contentType: 'LINK',
      externalUrl: 'https://example.com',
      fileKey: null,
      description: null,
      position: 0,
      createdAt: '',
      updatedAt: null,
    })

    const payload: contentApi.CreateContentPayload = {
      topicId: 't1',
      title: 'Aula 1',
      contentType: 'LINK',
      externalUrl: 'https://example.com',
    }

    const { result } = renderHook(() => useCreateContent('s1'), { wrapper })
    act(() => result.current.mutate(payload))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(contentApi.createContent).toHaveBeenCalledWith('s1', payload)
  })

  it('exposes error on failure', async () => {
    vi.mocked(contentApi.createContent).mockRejectedValue({ response: { status: 422 } })

    const { result } = renderHook(() => useCreateContent('s1'), { wrapper })
    act(() =>
      result.current.mutate({ topicId: 't1', title: '', contentType: 'LINK', externalUrl: 'bad' }),
    )

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
