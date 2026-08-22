import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect } from 'vitest'
import { PasswordInput } from './password-input'

describe('PasswordInput', () => {
  it('starts hidden and toggles the input type', async () => {
    const user = userEvent.setup()
    render(<PasswordInput aria-label="senha" />)

    const field = screen.getByLabelText('senha') as HTMLInputElement
    expect(field.type).toBe('password')

    await user.click(screen.getByRole('button', { name: 'Mostrar senha' }))
    expect(field.type).toBe('text')

    await user.click(screen.getByRole('button', { name: 'Ocultar senha' }))
    expect(field.type).toBe('password')
  })

  it('reflects the visibility state on the toggle button', async () => {
    const user = userEvent.setup()
    render(<PasswordInput aria-label="senha" />)

    const toggle = screen.getByRole('button', { name: 'Mostrar senha' })
    expect(toggle.getAttribute('aria-pressed')).toBe('false')

    await user.click(toggle)

    expect(toggle.getAttribute('aria-label')).toBe('Ocultar senha')
    expect(toggle.getAttribute('aria-pressed')).toBe('true')
  })

  it('keeps each instance independent', async () => {
    const user = userEvent.setup()
    render(
      <>
        <PasswordInput aria-label="nova senha" />
        <PasswordInput aria-label="confirmar senha" />
      </>
    )

    const first = screen.getByLabelText('nova senha') as HTMLInputElement
    const second = screen.getByLabelText('confirmar senha') as HTMLInputElement

    await user.click(screen.getAllByRole('button', { name: 'Mostrar senha' })[0])

    expect(first.type).toBe('text')
    expect(second.type).toBe('password')
  })

  it('does not submit the surrounding form', async () => {
    const user = userEvent.setup()
    let submitted = false
    render(
      <form onSubmit={() => { submitted = true }}>
        <PasswordInput aria-label="senha" />
      </form>
    )

    await user.click(screen.getByRole('button', { name: 'Mostrar senha' }))

    expect(submitted).toBe(false)
  })
})
