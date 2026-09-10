import { describe, it, expect } from 'vitest'
import { passwordSchema } from './passwordSchema'

function firstError(value: string): string | undefined {
  const result = passwordSchema.safeParse(value)
  return result.success ? undefined : result.error.issues[0]?.message
}

function messages(value: string): string[] {
  const result = passwordSchema.safeParse(value)
  return result.success ? [] : result.error.issues.map((issue) => issue.message)
}

describe('passwordSchema', () => {
  it('accepts a password that meets every criterion', () => {
    expect(passwordSchema.safeParse('Senha@123').success).toBe(true)
  })

  it('rejects a password shorter than 8 characters with its own message', () => {
    expect(firstError('Ab@1')).toBe('Senha deve ter no mínimo 8 caracteres')
  })

  it('rejects a password without an uppercase letter', () => {
    expect(messages('senha@123')).toContain('A senha precisa de: uma maiúscula')
  })

  it('rejects a password without a lowercase letter', () => {
    expect(messages('SENHA@123')).toContain('A senha precisa de: uma minúscula')
  })

  it('rejects a password without a digit', () => {
    expect(messages('SenhaSenha@')).toContain('A senha precisa de: um número')
  })

  it('rejects a password without a symbol', () => {
    expect(messages('Senha1234')).toContain('A senha precisa de: um símbolo')
  })

  it('lists every missing criterion in a single message', () => {
    expect(messages('senhasenha')).toContain('A senha precisa de: uma maiúscula, um número, um símbolo')
  })
})
