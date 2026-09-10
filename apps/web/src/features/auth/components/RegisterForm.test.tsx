import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import RegisterForm from './RegisterForm'

async function fill(fields: { password: string; confirmPassword: string }) {
  await userEvent.type(screen.getByLabelText('Nome completo'), 'Ana Souza')
  await userEvent.type(screen.getByLabelText(/e-mail/i), 'ana@test.com')
  await userEvent.type(screen.getByLabelText('Senha'), fields.password)
  await userEvent.type(screen.getByLabelText('Confirmar senha'), fields.confirmPassword)
  await userEvent.click(screen.getByRole('button', { name: /criar conta/i }))
}

describe('RegisterForm', () => {
  it('renders the confirm password field', () => {
    render(<RegisterForm onSubmit={vi.fn()} isPending={false} serverError={null} />)
    expect(screen.getByLabelText('Confirmar senha')).toBeTruthy()
  })

  it('blocks a weak password and lists the missing criteria', async () => {
    const onSubmit = vi.fn()
    render(<RegisterForm onSubmit={onSubmit} isPending={false} serverError={null} />)
    await fill({ password: 'senhasenha', confirmPassword: 'senhasenha' })
    await waitFor(() => {
      expect(screen.getByText('A senha precisa de: uma maiúscula, um número, um símbolo')).toBeTruthy()
    })
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('blocks mismatched passwords', async () => {
    const onSubmit = vi.fn()
    render(<RegisterForm onSubmit={onSubmit} isPending={false} serverError={null} />)
    await fill({ password: 'Senha@123', confirmPassword: 'Senha@124' })
    await waitFor(() => {
      expect(screen.getByText('As senhas não conferem')).toBeTruthy()
    })
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits when the password is strong and both fields match', async () => {
    const onSubmit = vi.fn()
    render(<RegisterForm onSubmit={onSubmit} isPending={false} serverError={null} />)
    await fill({ password: 'Senha@123', confirmPassword: 'Senha@123' })
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          fullName: 'Ana Souza',
          email: 'ana@test.com',
          password: 'Senha@123',
          confirmPassword: 'Senha@123',
        }),
        expect.anything(),
      )
    })
  })
})
