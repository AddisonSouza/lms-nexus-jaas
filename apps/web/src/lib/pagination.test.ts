import { describe, it, expect } from 'vitest'
import { z } from 'zod'
import { pageSchema } from './pagination'

const schema = pageSchema(z.object({ id: z.string() }))

describe('pageSchema', () => {
  it('parses the envelope the API contract defines', () => {
    const page = schema.parse({
      content: [{ id: 'a' }],
      totalElements: 3,
      totalPages: 2,
      number: 0,
      size: 2,
    })

    expect(page.content).toEqual([{ id: 'a' }])
    expect(page.totalElements).toBe(3)
    expect(page.totalPages).toBe(2)
  })

  it('rejects a bare array, which is what the endpoint used to answer', () => {
    expect(() => schema.parse([{ id: 'a' }])).toThrow()
  })

  it('rejects an envelope whose items have the wrong shape', () => {
    expect(() =>
      schema.parse({ content: [{ id: 1 }], totalElements: 1, totalPages: 1, number: 0, size: 20 })
    ).toThrow()
  })
})
