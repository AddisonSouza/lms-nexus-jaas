import { z } from 'zod'

const MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB

const baseContentSchema = z.object({
  title: z.string().min(2, 'Título deve ter ao menos 2 caracteres').max(255, 'Título muito longo'),
  topicId: z.string().min(1, 'Selecione um tópico'),
  description: z.string().max(2000, 'Descrição muito longa').optional().or(z.literal('')),
})

const urlContentSchema = baseContentSchema.extend({
  contentType: z.enum(['VIDEO', 'LINK'] as const),
  externalUrl: z.string().url('URL inválida'),
  file: z.undefined().optional(),
})

const fileContentSchema = baseContentSchema.extend({
  contentType: z.enum(['DOCUMENTO', 'ARQUIVO'] as const),
  externalUrl: z.undefined().optional(),
  file: z
    .instanceof(File, { message: 'Arquivo obrigatório' })
    .refine((f) => f.size <= MAX_FILE_SIZE, 'Arquivo deve ter no máximo 50MB'),
})

export const contentSchema = z.discriminatedUnion('contentType', [urlContentSchema, fileContentSchema])

export type ContentFormData = z.infer<typeof contentSchema>
