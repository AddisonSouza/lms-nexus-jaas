import { describe, it, expect } from 'vitest'
import { rateLimitSeconds, rateLimitMessage, rateLimitMessageFor } from './rateLimitSchema'

const blocked = (retryAfter?: string) => ({
  response: {
    status: 429,
    headers: retryAfter === undefined ? {} : { 'retry-after': retryAfter },
    data: { error: 'AUTH_RATE_LIMIT_EXCEEDED' },
  },
})

describe('rateLimitSeconds', () => {
  it('reads the Retry-After header of a 429', () => {
    expect(rateLimitSeconds(blocked('900'))).toBe(900)
  })

  it('ignores a header that is not a positive number', () => {
    expect(rateLimitSeconds(blocked('Wed, 21 Oct 2026 07:28:00 GMT'))).toBeNull()
    expect(rateLimitSeconds(blocked('0'))).toBeNull()
    expect(rateLimitSeconds(blocked('-5'))).toBeNull()
  })

  it('ignores anything that is not a 429', () => {
    expect(rateLimitSeconds({ response: { status: 401, headers: { 'retry-after': '900' } } })).toBeNull()
    expect(rateLimitSeconds(new Error('network down'))).toBeNull()
    expect(rateLimitSeconds(null)).toBeNull()
  })
})

describe('rateLimitMessage', () => {
  it('rounds up, so the user does not come back early and get refused again', () => {
    expect(rateLimitMessage(900)).toBe('Muitas tentativas. Tente novamente em 15 minutos.')
    expect(rateLimitMessage(61)).toBe('Muitas tentativas. Tente novamente em 2 minutos.')
  })

  it('never promises less than a minute', () => {
    expect(rateLimitMessage(1)).toBe('Muitas tentativas. Tente novamente em 1 minuto.')
  })
})

describe('rateLimitMessageFor', () => {
  it('says what happened even without a readable Retry-After', () => {
    expect(rateLimitMessageFor(blocked())).toBe(
      'Muitas tentativas. Aguarde alguns minutos e tente de novo.',
    )
  })

  it('leaves other refusals to the screen that called it', () => {
    expect(rateLimitMessageFor({ response: { status: 401, headers: {} } })).toBeNull()
  })
})
