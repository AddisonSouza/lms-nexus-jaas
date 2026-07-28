import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { Segmented } from './segmented'

const OPTIONS = [
  { value: 'all', label: 'Todas' },
  { value: 'draft', label: 'Rascunhos' },
]

describe('Segmented', () => {
  it('marks the active option as checked', () => {
    render(<Segmented value="draft" onValueChange={vi.fn()} options={OPTIONS} />)

    expect(screen.getByRole('radio', { name: 'Todas' }).getAttribute('aria-checked')).toBe('false')
    expect(screen.getByRole('radio', { name: 'Rascunhos' }).getAttribute('aria-checked')).toBe('true')
  })

  it('calls onValueChange with the clicked option value', async () => {
    const user = userEvent.setup()
    const onValueChange = vi.fn()
    render(<Segmented value="all" onValueChange={onValueChange} options={OPTIONS} />)

    await user.click(screen.getByRole('radio', { name: 'Rascunhos' }))

    expect(onValueChange).toHaveBeenCalledWith('draft')
  })
})
