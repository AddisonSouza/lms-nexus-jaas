import { z } from 'zod'
import api from '@lib/axios'

const switchOrganizationSchema = z.object({
  accessToken: z.string(),
})

/**
 * Reemite o access token na organização escolhida. Vive fora das features porque
 * é sessão, não organização: entrar numa organização acontece ao trocar pelo
 * seletor, ao criar uma e ao aceitar um convite — features diferentes que não
 * podem importar umas às outras.
 */
export async function switchOrganization(organizationId: string): Promise<string> {
  const response = await api.post(
    '/auth/switch-organization',
    { organizationId },
    { withCredentials: true },
  )
  return switchOrganizationSchema.parse(response.data).accessToken
}
