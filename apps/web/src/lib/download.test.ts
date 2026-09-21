import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { downloadFile, fileNameFromKey } from './download'
import api from './axios'

vi.mock('./axios', () => ({ default: { get: vi.fn() } }))

const BLOB = new Blob(['conteúdo'], { type: 'application/pdf' })

beforeEach(() => {
  vi.clearAllMocks()
  vi.useFakeTimers()
  URL.createObjectURL = vi.fn(() => 'blob:fake-url')
  URL.revokeObjectURL = vi.fn()
  vi.mocked(api.get).mockResolvedValue({ data: BLOB })
})

afterEach(() => {
  vi.useRealTimers()
})

describe('downloadFile', () => {
  it('asks the API for the file as a blob, so the token travels with it', async () => {
    await downloadFile('key-1', 'prova.pdf')

    expect(api.get).toHaveBeenCalledWith('/files/key-1', { responseType: 'blob' })
    expect(URL.createObjectURL).toHaveBeenCalledWith(BLOB)
  })

  it('saves it under the original name, not the opaque file key', async () => {
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})

    await downloadFile('key-1', 'prova final.pdf')

    const anchor = click.mock.instances[0] as unknown as HTMLAnchorElement
    expect(anchor.download).toBe('prova final.pdf')
    expect(anchor.getAttribute('href')).toBe('blob:fake-url')
    click.mockRestore()
  })

  it('leaves no anchor behind in the document', async () => {
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})

    await downloadFile('key-1', 'prova.pdf')

    expect(document.querySelectorAll('a[download]')).toHaveLength(0)
  })

  // Revogar antes do navegador terminar de ler aborta o download.
  it('revokes the object URL only after the browser has had time to read it', async () => {
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})

    await downloadFile('key-1', 'prova.pdf')
    expect(URL.revokeObjectURL).not.toHaveBeenCalled()

    vi.advanceTimersByTime(60_000)
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:fake-url')
  })

  it('propagates a failure so the caller can report it', async () => {
    vi.mocked(api.get).mockRejectedValue({ response: { status: 404 } })

    await expect(downloadFile('sumiu', 'prova.pdf')).rejects.toMatchObject({
      response: { status: 404 },
    })
    expect(URL.createObjectURL).not.toHaveBeenCalled()
  })
})

describe('fileNameFromKey', () => {
  it('drops the path and the uuid, keeping the name with its extension', () => {
    expect(fileNameFromKey('content/2026/09/0f1c9f6e-1a2b-4c3d-9e8f-abcdefabcdef-apostila.pdf'))
      .toBe('apostila.pdf')
  })

  it('falls back to the whole tail when the key has no uuid prefix', () => {
    expect(fileNameFromKey('content/2026/09/apostila.pdf')).toBe('apostila.pdf')
  })
})
