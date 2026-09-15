import { describe, it, expect } from 'vitest'
import { roleLabel } from './roles'

describe('roleLabel', () => {
  it('maps each member role to its readable label', () => {
    expect(roleLabel('ADMIN_ORG')).toBe('Administrador')
    expect(roleLabel('GESTOR')).toBe('Gestor')
    expect(roleLabel('PROFESSOR')).toBe('Professor')
    expect(roleLabel('ALUNO')).toBe('Aluno')
  })

  it('returns an unknown role as it came', () => {
    expect(roleLabel('OUTRO')).toBe('OUTRO')
  })
})
