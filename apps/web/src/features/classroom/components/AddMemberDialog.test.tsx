import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AddMemberDialog from './AddMemberDialog'

const onSubmit = vi.fn()
const onClose = vi.fn()

function renderDialog(isPending = false) {
  return render(
    <AddMemberDialog open onClose={onClose} onSubmit={onSubmit} isPending={isPending} />,
  )
}

beforeEach(() => vi.clearAllMocks())

describe('AddMemberDialog', () => {
  it('asks for the role in plain words instead of leaking the validation library message', async () => {
    renderDialog()

    await userEvent.type(screen.getByLabelText('ID do usuário *'), 'user-1')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar' }))

    expect(await screen.findByText('Selecione o papel')).toBeTruthy()
    expect(screen.queryByText(/Invalid enum value/)).toBeNull()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('does not submit without a user id', async () => {
    renderDialog()

    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar' }))

    expect(await screen.findByText('Selecione um membro')).toBeTruthy()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits the user id and the chosen role', async () => {
    renderDialog()

    await userEvent.type(screen.getByLabelText('ID do usuário *'), 'user-1')
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'PROFESSOR')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ userId: 'user-1', role: 'PROFESSOR' })
  })

  it('clears the form when cancelled', async () => {
    renderDialog()

    await userEvent.type(screen.getByLabelText('ID do usuário *'), 'user-1')
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(onClose).toHaveBeenCalled()
    expect((screen.getByLabelText('ID do usuário *') as HTMLInputElement).value).toBe('')
  })

  it('blocks a second submit while one is pending', () => {
    renderDialog(true)

    expect((screen.getByRole('button', { name: 'Adicionar' }) as HTMLButtonElement).disabled).toBe(true)
  })
})
