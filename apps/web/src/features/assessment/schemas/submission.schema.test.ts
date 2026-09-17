import { describe, it, expect } from 'vitest'
import { submissionSchema } from './submission.schema'

function file(name: string, type: string) {
  return new File(['conteudo'], name, { type })
}

function validate(...files: File[]) {
  return submissionSchema.safeParse({ textResponse: 'Minha resposta', files })
}

describe('submissionSchema — tipos de anexo', () => {
  it.each([
    ['resposta.pdf', 'application/pdf'],
    ['resposta.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
    ['resposta.zip', 'application/x-zip-compressed'],
    ['foto.JPG', 'image/jpeg'],
    ['foto.png', 'image/png'],
  ])('accepts %s', (name, type) => {
    expect(validate(file(name, type)).success).toBe(true)
  })

  it('refuses an executable, which is the whole point', () => {
    expect(validate(file('virus.exe', 'application/x-msdownload')).success).toBe(false)
  })

  it('refuses a forbidden extension even when the browser reports no MIME type', () => {
    // No Linux o `File.type` de um .exe costuma vir vazio: só o MIME deixaria passar.
    expect(validate(file('virus.exe', '')).success).toBe(false)
  })

  it('accepts an allowed extension when the browser reports no MIME type', () => {
    expect(validate(file('resposta.pdf', '')).success).toBe(true)
  })

  it('refuses a file with no extension at all', () => {
    expect(validate(file('resposta', 'application/pdf')).success).toBe(false)
  })

  it('refuses the whole batch when one file is not allowed', () => {
    const result = validate(file('resposta.pdf', 'application/pdf'), file('virus.exe', ''))

    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0].message).toContain('Tipo de arquivo não permitido')
    }
  })

  it('still requires a text or a file', () => {
    expect(submissionSchema.safeParse({ textResponse: '   ', files: [] }).success).toBe(false)
  })

  it('accepts a text-only submission', () => {
    expect(submissionSchema.safeParse({ textResponse: 'Minha resposta', files: [] }).success).toBe(true)
  })
})
