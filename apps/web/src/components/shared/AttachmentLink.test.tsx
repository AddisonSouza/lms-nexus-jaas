import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AttachmentLink, { formatFileSize } from './AttachmentLink'
import { downloadFile } from '@lib/download'

vi.mock('@lib/download', () => ({ downloadFile: vi.fn() }))

beforeEach(() => {
  vi.clearAllMocks()
})

describe('formatFileSize', () => {
  it('scales the unit so the number stays readable', () => {
    expect(formatFileSize(512)).toBe('512 B')
    expect(formatFileSize(2048)).toBe('2,0 KB')
    expect(formatFileSize(1_500_000)).toBe('1,4 MB')
  })
})

describe('AttachmentLink', () => {
  it('names the file and its size', () => {
    render(<AttachmentLink fileKey="k-1" originalName="prova.pdf" sizeBytes={2048} />)

    expect(screen.getByText('prova.pdf')).toBeTruthy()
    expect(screen.getByText('(2,0 KB)')).toBeTruthy()
  })

  it('downloads through the authenticated client on click', async () => {
    vi.mocked(downloadFile).mockResolvedValue(undefined)
    render(<AttachmentLink fileKey="k-1" originalName="prova.pdf" />)

    await userEvent.click(screen.getByRole('button', { name: /baixar prova\.pdf/i }))

    expect(downloadFile).toHaveBeenCalledWith('k-1', 'prova.pdf')
  })

  it('disables the button while the file is on its way', async () => {
    let release: () => void = () => {}
    vi.mocked(downloadFile).mockReturnValue(new Promise<void>((r) => { release = r }))
    render(<AttachmentLink fileKey="k-1" originalName="prova.pdf" />)

    const button = screen.getByRole('button', { name: /baixar prova\.pdf/i })
    await userEvent.click(button)

    expect((button as HTMLButtonElement).disabled).toBe(true)

    release()
    await waitFor(() => expect((button as HTMLButtonElement).disabled).toBe(false))
  })

  // Sem isto o clique falha em silêncio e o arquivo simplesmente não aparece.
  it('reports why the download failed instead of failing silently', async () => {
    vi.mocked(downloadFile).mockRejectedValue({ response: { status: 404 } })
    render(<AttachmentLink fileKey="sumiu" originalName="prova.pdf" />)

    await userEvent.click(screen.getByRole('button', { name: /baixar prova\.pdf/i }))

    expect(await screen.findByRole('alert')).toBeTruthy()
  })
})
