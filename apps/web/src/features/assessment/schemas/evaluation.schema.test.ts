import { describe, it, expect } from 'vitest'
import { createEvaluationSchema } from './evaluation.schema'

describe('createEvaluationSchema', () => {
  it('rejects a grade above the task maximum', () => {
    const result = createEvaluationSchema(10).safeParse({ grade: 11, feedback: 'ok' })

    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].message).toBe('Nota não pode exceder 10')
    }
  })

  it('accepts a grade at the maximum', () => {
    expect(createEvaluationSchema(10).safeParse({ grade: 10, feedback: 'ok' }).success).toBe(true)
  })

  it('still rejects a negative grade', () => {
    const result = createEvaluationSchema(10).safeParse({ grade: -1, feedback: 'ok' })

    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].message).toBe('Nota não pode ser negativa')
    }
  })

  it('accepts any grade when the task has no maximum', () => {
    expect(createEvaluationSchema(null).safeParse({ grade: 999, feedback: 'ok' }).success).toBe(true)
  })

  it('keeps an empty grade as null, since the grade is optional', () => {
    const result = createEvaluationSchema(10).safeParse({ grade: '', feedback: 'ok' })

    expect(result.success).toBe(true)
    if (result.success) expect(result.data.grade).toBeNull()
  })
})
