import { z } from 'zod'

// Convites são emitidos como UUID (InviteMemberService), e o e-mail leva o link
// completo para /invitations/<token>/accept. Aceitamos os dois: o link colado da
// mensagem ou só o token, para o usuário não precisar recortar nada.
const INVITE_TOKEN_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
const INVITE_LINK_PATTERN = /invitations\/([^/?#\s]+)(?:\/accept)?/i

/** Extrai o token de um link de convite colado, ou `null` se não houver um válido. */
export function extractInviteToken(input: string): string | null {
  const value = input.trim()
  if (!value) return null

  const candidate = value.match(INVITE_LINK_PATTERN)?.[1] ?? value
  return INVITE_TOKEN_PATTERN.test(candidate) ? candidate.toLowerCase() : null
}

export const inviteLinkSchema = z.object({
  inviteLink: z
    .string()
    .min(1, 'Cole o link do convite que você recebeu por e-mail')
    .refine(
      (value) => extractInviteToken(value) !== null,
      'Link de convite inválido. Cole o link completo recebido por e-mail.',
    ),
})

export type InviteLinkFormData = z.infer<typeof inviteLinkSchema>
