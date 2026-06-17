import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import PeriodSelector from './PeriodSelector'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('PeriodSelector', () => {
  it('calls onChange with a 30-day range when the "30 dias" shortcut is clicked', async () => {
    const onChange = vi.fn()
    render(<PeriodSelector value={{ from: '2026-01-01', to: '2026-01-31' }} onChange={onChange} />)

    await userEvent.click(screen.getByRole('button', { name: '30 dias' }))

    expect(onChange).toHaveBeenCalledTimes(1)
    const [period] = onChange.mock.calls[0]
    expect(period.from <= period.to).toBe(true)
  })

  it('submits the custom range when both dates are valid', async () => {
    const onChange = vi.fn()
    render(<PeriodSelector value={{ from: '2026-01-01', to: '2026-01-31' }} onChange={onChange} />)

    const fromInput = screen.getByLabelText('De') as HTMLInputElement
    const toInput = screen.getByLabelText('Até') as HTMLInputElement
    await userEvent.clear(fromInput)
    await userEvent.type(fromInput, '2026-02-01')
    await userEvent.clear(toInput)
    await userEvent.type(toInput, '2026-02-28')
    await userEvent.click(screen.getByRole('button', { name: 'Aplicar' }))

    await waitFor(() => {
      expect(onChange).toHaveBeenCalledWith({ from: '2026-02-01', to: '2026-02-28' })
    })
  })

  it('shows a validation error when "from" is after "to"', async () => {
    const onChange = vi.fn()
    render(<PeriodSelector value={{ from: '2026-01-01', to: '2026-01-31' }} onChange={onChange} />)

    const fromInput = screen.getByLabelText('De') as HTMLInputElement
    const toInput = screen.getByLabelText('Até') as HTMLInputElement
    await userEvent.clear(fromInput)
    await userEvent.type(fromInput, '2026-03-01')
    await userEvent.clear(toInput)
    await userEvent.type(toInput, '2026-01-01')
    await userEvent.click(screen.getByRole('button', { name: 'Aplicar' }))

    await waitFor(() => {
      expect(screen.getByText(/não pode ser posterior/i)).toBeTruthy()
    })
    expect(onChange).not.toHaveBeenCalled()
  })
})
