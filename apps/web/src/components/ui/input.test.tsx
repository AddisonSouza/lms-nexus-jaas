import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect } from 'vitest'
import { Input } from './input'
import { Textarea } from './textarea'

describe('Input', () => {
  it('accepts typed text', async () => {
    const user = userEvent.setup()
    render(<Input aria-label="e-mail" />)

    await user.type(screen.getByLabelText('e-mail'), 'ana@escola.br')

    expect((screen.getByLabelText('e-mail') as HTMLInputElement).value).toBe('ana@escola.br')
  })

  it('cannot be typed into when disabled', async () => {
    const user = userEvent.setup()
    render(<Input aria-label="e-mail" disabled />)

    await user.type(screen.getByLabelText('e-mail'), 'ana@escola.br')

    expect((screen.getByLabelText('e-mail') as HTMLInputElement).value).toBe('')
  })
})

describe('Textarea', () => {
  it('accepts typed text', async () => {
    const user = userEvent.setup()
    render(<Textarea aria-label="resposta" />)

    await user.type(screen.getByLabelText('resposta'), 'minha resposta')

    expect((screen.getByLabelText('resposta') as HTMLTextAreaElement).value).toBe('minha resposta')
  })
})
