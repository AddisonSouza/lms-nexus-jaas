import { z } from 'zod'

export const topicSchema = z.object({
  title: z.string().min(2, 'Título deve ter ao menos 2 caracteres').max(255, 'Título muito longo'),
})

export type TopicFormData = z.infer<typeof topicSchema>
