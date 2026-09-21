import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ContentCard from './ContentCard'
import { downloadFile } from '@lib/download'
import type { SubjectContent } from '../types'

vi.mock('@lib/download', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@lib/download')>()
  return { ...actual, downloadFile: vi.fn() }
})

const BASE: SubjectContent = {
  id: 'c-1',
  topicId: 't-1',
  organizationId: 'org-1',
  title: 'Apostila',
  contentType: 'DOCUMENTO',
  externalUrl: null,
  fileKey: null,
  description: null,
  position: 0,
  createdAt: '2026-09-01T10:00:00',
  updatedAt: null,
}

const noop = () => {}

function renderCard(content: SubjectContent) {
  return render(
    <ContentCard content={content} canManage={false} onEdit={noop} onDelete={noop} />,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('ContentCard', () => {
  // Um `<a href>` para /files não leva o JWT: a aba abria em 401 em vez do arquivo.
  it('downloads a stored file through the authenticated client', async () => {
    vi.mocked(downloadFile).mockResolvedValue(undefined)
    renderCard({
      ...BASE,
      fileKey: 'content/2026/09/0f1c9f6e-1a2b-4c3d-9e8f-abcdefabcdef-apostila.pdf',
    })

    await userEvent.click(screen.getByRole('button', { name: /baixar apostila/i }))

    expect(downloadFile).toHaveBeenCalledWith(
      'content/2026/09/0f1c9f6e-1a2b-4c3d-9e8f-abcdefabcdef-apostila.pdf',
      'apostila.pdf',
    )
  })

  it('keeps an external link as a plain link, with no download', async () => {
    renderCard({ ...BASE, contentType: 'LINK', externalUrl: 'https://khanacademy.org' })

    const link = screen.getByRole('link', { name: /abrir apostila/i })
    expect(link.getAttribute('href')).toBe('https://khanacademy.org')
    expect(link.getAttribute('target')).toBe('_blank')
    expect(screen.queryByRole('button', { name: /baixar/i })).toBeNull()
    expect(downloadFile).not.toHaveBeenCalled()
  })

  it('offers nothing to open when the content has neither file nor link', () => {
    renderCard(BASE)

    expect(screen.queryByRole('button', { name: /baixar/i })).toBeNull()
    expect(screen.queryByRole('link')).toBeNull()
  })
})
