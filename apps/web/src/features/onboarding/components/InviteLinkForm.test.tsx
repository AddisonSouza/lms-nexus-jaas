import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import InviteLinkForm from './InviteLinkForm'

const TOKEN = '3f2504e0-4f89-11d3-9a0c-0305e82c3301'

function renderForm() {
  return render(
    <MemoryRouter initialEntries={['/welcome']}>
      <Routes>
        <Route path="/welcome" element={<InviteLinkForm />} />
        <Route path="/invitations/:token/accept" element={<AcceptStub />} />
      </Routes>
    </MemoryRouter>,
  )
}

function AcceptStub() {
  return <div>accept screen</div>
}

describe('InviteLinkForm', () => {
  it('navigates to the accept screen with the token from the pasted link', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Link do convite'), `https://lms.app/invitations/${TOKEN}/accept`)
    await user.click(screen.getByRole('button', { name: 'Entrar na organização' }))

    await waitFor(() => expect(screen.getByText('accept screen')).toBeTruthy())
  })

  it('shows a validation error and stays put when the link is invalid', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Link do convite'), 'nao-e-um-convite')
    await user.click(screen.getByRole('button', { name: 'Entrar na organização' }))

    await waitFor(() =>
      expect(screen.getByRole('alert').textContent).toBe(
        'Link de convite inválido. Cole o link completo recebido por e-mail.',
      ),
    )
    expect(screen.queryByText('accept screen')).toBeNull()
  })
})
