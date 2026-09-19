import { describe, it, expect } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { getRegisterError } from './useRegister'

function axiosError(status: number, data: unknown, headers: Record<string, string> = {}) {
  const error = new AxiosError('failed')
  error.response = {
    status,
    data,
    statusText: '',
    headers,
    config: { headers: new AxiosHeaders() },
  }
  return error
}

describe('getRegisterError', () => {
  it('tells the user to wait when the IP is blocked, instead of blaming the form', () => {
    const message = getRegisterError(
      axiosError(429, { error: 'AUTH_RATE_LIMIT_EXCEEDED' }, { 'retry-after': '900' }),
    )

    expect(message).toBe('Muitas tentativas. Tente novamente em 15 minutos.')
  })

  it('still reports an email already in use', () => {
    expect(getRegisterError(axiosError(409, { error: 'E-mail já em uso' }))).toBe('E-mail já em uso')
  })

  it('still lists the validation failures', () => {
    expect(getRegisterError(axiosError(422, { errors: ['senha fraca', 'e-mail inválido'] })))
      .toBe('senha fraca, e-mail inválido')
  })

  it('ignores anything that is not an API refusal', () => {
    expect(getRegisterError(new Error('network down'))).toBeNull()
  })
})
