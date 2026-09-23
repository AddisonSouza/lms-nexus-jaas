import { act, renderHook } from '@testing-library/react'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useDebouncedValue } from './useDebouncedValue'

beforeEach(() => {
  vi.useFakeTimers()
})

afterEach(() => {
  vi.useRealTimers()
})

describe('useDebouncedValue', () => {
  it('returns the initial value without waiting', () => {
    const { result } = renderHook(() => useDebouncedValue('ana', 300))

    expect(result.current).toBe('ana')
  })

  it('holds the new value until the delay has passed', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value, 300), {
      initialProps: { value: 'ana' },
    })

    rerender({ value: 'bruno' })
    expect(result.current).toBe('ana')

    act(() => vi.advanceTimersByTime(299))
    expect(result.current).toBe('ana')

    act(() => vi.advanceTimersByTime(1))
    expect(result.current).toBe('bruno')
  })

  it('restarts the wait on every keystroke, so only the last value lands', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value, 300), {
      initialProps: { value: 'a' },
    })

    // Digitação contínua: nenhuma das parciais pode chegar ao servidor.
    rerender({ value: 'an' })
    act(() => vi.advanceTimersByTime(200))
    rerender({ value: 'ana' })
    act(() => vi.advanceTimersByTime(200))

    expect(result.current).toBe('a')

    act(() => vi.advanceTimersByTime(100))
    expect(result.current).toBe('ana')
  })

  it('drops the pending value when the component unmounts', () => {
    const { result, rerender, unmount } = renderHook(
      ({ value }) => useDebouncedValue(value, 300),
      { initialProps: { value: 'ana' } }
    )

    rerender({ value: 'bruno' })
    unmount()

    act(() => vi.advanceTimersByTime(300))
    expect(result.current).toBe('ana')
  })
})
