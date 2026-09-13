import { useState } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { DateTimeField } from './datetime-field'

function Controlled({ initial = '', onValue }: { initial?: string; onValue: (v: string) => void }) {
  const [value, setValue] = useState(initial)
  return (
    <DateTimeField
      value={value}
      onChange={(next) => {
        setValue(next)
        onValue(next)
      }}
    />
  )
}

describe('DateTimeField', () => {
  it('sugere 23:59 quando a data é escolhida e a hora está vazia', async () => {
    const user = userEvent.setup()
    const onValue = vi.fn()
    render(<Controlled onValue={onValue} />)

    await user.type(screen.getByLabelText('Data'), '2026-09-20')

    expect(onValue).toHaveBeenLastCalledWith('2026-09-20T23:59')
    expect((screen.getByLabelText('Hora') as HTMLInputElement).value).toBe('23:59')
  })

  it('combina data e hora sem segundos', async () => {
    const user = userEvent.setup()
    const onValue = vi.fn()
    render(<Controlled onValue={onValue} />)

    await user.type(screen.getByLabelText('Data'), '2026-09-20')
    await user.clear(screen.getByLabelText('Hora'))
    await user.type(screen.getByLabelText('Hora'), '14:30')

    expect(onValue).toHaveBeenLastCalledWith('2026-09-20T14:30')
  })

  it('não forma valor enquanto só a hora estiver preenchida', async () => {
    const user = userEvent.setup()
    const onValue = vi.fn()
    render(<Controlled onValue={onValue} />)

    await user.type(screen.getByLabelText('Hora'), '14:30')

    expect(onValue).toHaveBeenLastCalledWith('')
  })

  it('mostra data e hora separadas a partir de um valor existente', () => {
    render(<Controlled initial="2026-09-20T14:30" onValue={vi.fn()} />)

    expect((screen.getByLabelText('Data') as HTMLInputElement).value).toBe('2026-09-20')
    expect((screen.getByLabelText('Hora') as HTMLInputElement).value).toBe('14:30')
  })

  it('limpa os dois inputs quando o valor é zerado de fora', () => {
    const { rerender } = render(
      <DateTimeField value="2026-09-20T14:30" onChange={vi.fn()} />
    )
    rerender(<DateTimeField value="" onChange={vi.fn()} />)

    expect((screen.getByLabelText('Data') as HTMLInputElement).value).toBe('')
    expect((screen.getByLabelText('Hora') as HTMLInputElement).value).toBe('')
  })
})
