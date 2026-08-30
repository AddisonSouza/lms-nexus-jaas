import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import ListErrorState from './ListErrorState'

describe('ListErrorState', () => {
  it('announces the failure and names what failed to load', () => {
    render(<ListErrorState subject="as turmas" onRetry={vi.fn()} />)

    expect(screen.getByRole('alert')).toBeTruthy()
    expect(screen.getByText('Não foi possível carregar as turmas')).toBeTruthy()
  })

  it('retries on click', async () => {
    const user = userEvent.setup()
    const onRetry = vi.fn()
    render(<ListErrorState subject="as disciplinas" onRetry={onRetry} />)

    await user.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(onRetry).toHaveBeenCalledTimes(1)
  })

  it('blocks a second retry while one is in flight', async () => {
    const user = userEvent.setup()
    const onRetry = vi.fn()
    render(<ListErrorState subject="as turmas" onRetry={onRetry} isRetrying />)

    const button = screen.getByRole('button', { name: 'Tentando...' })
    await user.click(button)

    expect(onRetry).not.toHaveBeenCalled()
  })
})
