import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import LoginForm from './LoginForm'

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <MemoryRouter>{children}</MemoryRouter>
)

describe('LoginForm', () => {
  it('renders email and password fields', () => {
    render(<LoginForm onSubmit={vi.fn()} isPending={false} />, { wrapper })
    expect(screen.getByLabelText(/e-mail/i)).toBeTruthy()
    expect(screen.getByLabelText('Senha')).toBeTruthy()
  })

  it('shows validation errors for empty submit', async () => {
    render(<LoginForm onSubmit={vi.fn()} isPending={false} />, { wrapper })
    await userEvent.click(screen.getByRole('button', { name: /entrar/i }))
    await waitFor(() => {
      expect(screen.getByText(/e-mail é obrigatório/i)).toBeTruthy()
    })
  })

  it('calls onSubmit with form data when valid', async () => {
    const onSubmit = vi.fn()
    render(<LoginForm onSubmit={onSubmit} isPending={false} />, { wrapper })
    await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
    await userEvent.type(screen.getByLabelText('Senha'), 'password123')
    await userEvent.click(screen.getByRole('button', { name: /entrar/i }))
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({ email: 'user@test.com', password: 'password123' })
    })
  })
})
