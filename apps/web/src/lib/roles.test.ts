import { describe, it, expect } from 'vitest'
import { canDeleteSubject, canJoinByCode, canManageSubject, roleLabel } from './roles'

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

const ROLES = ['ADMIN_ORG', 'GESTOR', 'PROFESSOR', 'ALUNO'] as const

describe('canDeleteSubject', () => {
  it('allows only the organization administrator', () => {
    expect(ROLES.filter(canDeleteSubject)).toEqual(['ADMIN_ORG'])
  })

  it('denies an absent role', () => {
    expect(canDeleteSubject(null)).toBe(false)
  })
})

describe('canManageSubject', () => {
  it('allows the administrator and the manager', () => {
    expect(ROLES.filter(canManageSubject)).toEqual(['ADMIN_ORG', 'GESTOR'])
  })

  it('denies an absent role', () => {
    expect(canManageSubject(null)).toBe(false)
  })
})

describe('canJoinByCode', () => {
  it('allows only the student', () => {
    expect(ROLES.filter(canJoinByCode)).toEqual(['ALUNO'])
  })

  it('denies an absent role', () => {
    expect(canJoinByCode(null)).toBe(false)
  })
})
