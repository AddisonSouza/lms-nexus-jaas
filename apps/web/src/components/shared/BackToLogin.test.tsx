import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import BackToLogin from './BackToLogin'

describe('BackToLogin', () => {
  it('links back to the login screen', () => {
    render(<BackToLogin />, { wrapper: MemoryRouter })

    const link = screen.getByRole('link', { name: 'Voltar ao login' })
    expect(link.getAttribute('href')).toBe('/login')
  })

  it('takes an extra class for screens that center their content', () => {
    render(<BackToLogin className="self-start" />, { wrapper: MemoryRouter })

    expect(screen.getByRole('link', { name: 'Voltar ao login' }).className).toContain('self-start')
  })
})
