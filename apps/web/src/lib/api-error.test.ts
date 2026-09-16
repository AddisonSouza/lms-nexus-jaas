import { describe, it, expect } from 'vitest'
import { apiErrorMessage } from './api-error'

function axiosError(status: number, data?: unknown) {
  return { response: { status, data } }
}

describe('apiErrorMessage', () => {
  it('translates a known business error code', () => {
    expect(apiErrorMessage(axiosError(403, { error: 'TASK_FORBIDDEN' }))).toBe(
      'Você não tem permissão para esta tarefa.'
    )
    expect(apiErrorMessage(axiosError(403, { error: 'INVITATION_NOT_FOR_THIS_USER' }))).toBe(
      'Este convite foi enviado para outro e-mail. Entre com a conta convidada.'
    )
  })

  it('lets the calling screen override a code', () => {
    const message = apiErrorMessage(axiosError(403, { error: 'TASK_FORBIDDEN' }), {
      TASK_FORBIDDEN: 'Só o professor da disciplina publica esta tarefa.',
    })
    expect(message).toBe('Só o professor da disciplina publica esta tarefa.')
  })

  it('joins the 422 validation messages without the field prefix', () => {
    const error = axiosError(422, {
      errors: [
        'resetPassword.arg0.newPassword: A senha precisa de: letra maiúscula, símbolo',
        'resetPassword.arg0.email: não deve estar em branco',
      ],
    })
    expect(apiErrorMessage(error)).toBe(
      'A senha precisa de: letra maiúscula, símbolo não deve estar em branco'
    )
  })

  it('hides the mime type behind a readable message on a rejected upload', () => {
    const error = axiosError(422, { error: 'File type not allowed: application/x-msdownload' })
    expect(apiErrorMessage(error)).toBe('Este tipo de arquivo não é permitido.')
  })

  it('shows a free-text error from the API as it came', () => {
    const error = axiosError(422, { error: 'Token inválido, expirado ou já utilizado' })
    expect(apiErrorMessage(error)).toBe('Token inválido, expirado ou já utilizado')
  })

  it('falls back to the status when the code is unknown', () => {
    expect(apiErrorMessage(axiosError(403, { error: 'SOME_NEW_CODE' }))).toBe(
      'Você não tem permissão para esta ação.'
    )
    expect(apiErrorMessage(axiosError(401, { error: 'Unauthorized' }))).toBe(
      'Sua sessão expirou. Entre de novo para continuar.'
    )
  })

  it('falls back to the status when there is no body', () => {
    expect(apiErrorMessage(axiosError(422))).toBe(
      'Dados inválidos. Revise os campos e tente de novo.'
    )
    expect(apiErrorMessage(axiosError(409, { unexpected: true }))).toBe(
      'Esta ação conflita com o estado atual. Recarregue a página.'
    )
  })

  it('falls back to the generic message without a status or without an error', () => {
    expect(apiErrorMessage(new Error('network down'))).toBe(
      'Não foi possível concluir a ação. Tente de novo em instantes.'
    )
    expect(apiErrorMessage(null)).toBe(
      'Não foi possível concluir a ação. Tente de novo em instantes.'
    )
  })
})
