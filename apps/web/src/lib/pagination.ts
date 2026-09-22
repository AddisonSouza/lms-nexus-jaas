import { z } from 'zod'

/**
 * O envelope paginado que o contrato define para listagens:
 * `{ content, totalElements, totalPages, number, size }`.
 *
 * É uma fábrica porque cada listagem carrega um item diferente — o schema do
 * item entra aqui e a validação do envelope vem de graça.
 */
export function pageSchema<T extends z.ZodTypeAny>(item: T) {
  return z.object({
    content: z.array(item),
    totalElements: z.number(),
    totalPages: z.number(),
    number: z.number(),
    size: z.number(),
  })
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

/** Busca e recorte que as listagens paginadas aceitam como query string. */
export interface PageParams {
  search?: string
  page?: number
  size?: number
}
