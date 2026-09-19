import { z } from 'zod'

/**
 * O `Retry-After` do 429 chega como texto do header. Em segundos, segundo o
 * HTTP — a forma com data não é usada pela API, e um valor que não seja um
 * número positivo é ignorado em vez de virar "NaN minutos" na tela.
 */
const retryAfterSchema = z.coerce.number().int().positive()

function status(error: unknown): number | undefined {
  return (error as { response?: { status?: number } })?.response?.status
}

/** Segundos que faltam do bloqueio, ou `null` se a recusa não for um 429 legível. */
export function rateLimitSeconds(error: unknown): number | null {
  if (status(error) !== 429) return null

  const headers = (error as { response?: { headers?: Record<string, unknown> } })
    .response?.headers
  const parsed = retryAfterSchema.safeParse(headers?.['retry-after'] ?? headers?.['Retry-After'])
  return parsed.success ? parsed.data : null
}

/**
 * "Tente novamente em N minutos" — arredondado para cima, porque prometer menos
 * tempo do que falta faz o usuário tentar cedo e levar outro 429. Abaixo de um
 * minuto vira "1 minuto": o bloqueio real do back é de 15.
 */
export function rateLimitMessage(seconds: number): string {
  const minutes = Math.max(1, Math.ceil(seconds / 60))
  return `Muitas tentativas. Tente novamente em ${minutes} ${minutes === 1 ? 'minuto' : 'minutos'}.`
}

/**
 * Mensagem de bloqueio para uma recusa da API, ou `null` quando ela não é um
 * 429 — a tela então segue com o texto que já tinha. Sem o `Retry-After`
 * legível (proxy que o remove, CORS mal configurado) ainda dá para dizer o que
 * houve, só não por quanto tempo.
 */
export function rateLimitMessageFor(error: unknown): string | null {
  if (status(error) !== 429) return null

  const seconds = rateLimitSeconds(error)
  return seconds === null
    ? 'Muitas tentativas. Aguarde alguns minutos e tente de novo.'
    : rateLimitMessage(seconds)
}
