import { describe, it, expect } from 'vitest'
import { extractInviteToken, inviteLinkSchema } from './inviteLinkSchema'

const TOKEN = '3f2504e0-4f89-11d3-9a0c-0305e82c3301'

describe('extractInviteToken', () => {
  it('extracts the token from the full link sent by email', () => {
    expect(extractInviteToken(`https://lms.app/invitations/${TOKEN}/accept`)).toBe(TOKEN)
  })

  it('accepts the bare token', () => {
    expect(extractInviteToken(TOKEN)).toBe(TOKEN)
  })

  it('ignores surrounding whitespace and query strings', () => {
    expect(extractInviteToken(`  https://lms.app/invitations/${TOKEN}/accept?src=email  `)).toBe(TOKEN)
  })

  it('accepts a link without the /accept suffix', () => {
    expect(extractInviteToken(`https://lms.app/invitations/${TOKEN}`)).toBe(TOKEN)
  })

  it('normalizes an uppercase token', () => {
    expect(extractInviteToken(TOKEN.toUpperCase())).toBe(TOKEN)
  })

  it('rejects a link whose token is not a UUID', () => {
    expect(extractInviteToken('https://lms.app/invitations/abc123/accept')).toBeNull()
  })

  it('rejects arbitrary text', () => {
    expect(extractInviteToken('qualquer coisa')).toBeNull()
  })

  it('rejects an empty value', () => {
    expect(extractInviteToken('   ')).toBeNull()
  })
})

describe('inviteLinkSchema', () => {
  it('accepts a valid invite link', () => {
    const result = inviteLinkSchema.safeParse({
      inviteLink: `https://lms.app/invitations/${TOKEN}/accept`,
    })

    expect(result.success).toBe(true)
  })

  it('asks the user to paste the link when the field is empty', () => {
    const result = inviteLinkSchema.safeParse({ inviteLink: '' })

    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].message).toBe(
        'Cole o link do convite que você recebeu por e-mail',
      )
    }
  })

  it('reports an invalid link', () => {
    const result = inviteLinkSchema.safeParse({ inviteLink: 'https://lms.app/invitations/abc/accept' })

    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].message).toBe(
        'Link de convite inválido. Cole o link completo recebido por e-mail.',
      )
    }
  })
})
