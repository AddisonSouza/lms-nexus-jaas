import { useState } from 'react'
import { FileText, Video, Link2, Archive, ExternalLink, Download, Loader2 } from 'lucide-react'
import type { SubjectContent, ContentType } from '../types'
import { Badge } from '@components/ui/badge'
import { downloadFile, fileNameFromKey } from '@lib/download'

const ICONS: Record<ContentType, React.ElementType> = {
  VIDEO: Video,
  DOCUMENTO: FileText,
  LINK: Link2,
  ARQUIVO: Archive,
}

const LABELS: Record<ContentType, string> = {
  VIDEO: 'Vídeo',
  DOCUMENTO: 'Documento',
  LINK: 'Link',
  ARQUIVO: 'Arquivo',
}

const ICON_STYLES: Record<ContentType, string> = {
  VIDEO: 'bg-accent-100 text-accent-800',
  DOCUMENTO: 'bg-accent-2-100 text-accent-2-800',
  LINK: 'bg-neutral-100 text-neutral-800',
  ARQUIVO: 'bg-neutral-100 text-neutral-800',
}

interface Props {
  content: SubjectContent
  canManage: boolean
  onEdit: (content: SubjectContent) => void
  onDelete: (contentId: string) => void
}

function ContentCard({ content, canManage, onEdit, onDelete }: Props) {
  const Icon = ICONS[content.contentType]
  const [isDownloading, setIsDownloading] = useState(false)

  const fileKey = content.fileKey

  // Arquivo do storage exige o JWT, então é um botão que baixa pelo `api`; o
  // `<a href>` de antes abria uma aba em 401. Link externo continua link.
  async function handleDownload() {
    if (!fileKey) return
    setIsDownloading(true)
    try {
      await downloadFile(fileKey, fileNameFromKey(fileKey))
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <div className="flex items-center gap-3 rounded-[var(--radius-md)] px-3 py-2 hover:bg-[color-mix(in_srgb,var(--color-text)_4%,transparent)]">
      <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${ICON_STYLES[content.contentType]}`}>
        <Icon className="h-4 w-4" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="truncate text-sm">{content.title}</p>
        {content.description && (
          <p className="mt-0.5 text-xs text-muted-foreground line-clamp-2">{content.description}</p>
        )}
      </div>

      <Badge variant="neutral">{LABELS[content.contentType]}</Badge>

      <div className="flex shrink-0 items-center gap-2">
        {fileKey && (
          <button
            type="button"
            onClick={handleDownload}
            disabled={isDownloading}
            className="text-muted-foreground hover:text-foreground disabled:opacity-60"
            aria-label={`Baixar ${content.title}`}
            title="Download"
          >
            {isDownloading ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Download className="h-4 w-4" />
            )}
          </button>
        )}
        {!fileKey && content.externalUrl && (
          <a
            href={content.externalUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-muted-foreground hover:text-foreground"
            aria-label={`Abrir ${content.title}`}
            title="Abrir link"
          >
            <ExternalLink className="h-4 w-4" />
          </a>
        )}
        {canManage && (
          <>
            <button
              onClick={() => onEdit(content)}
              className="text-xs text-muted-foreground hover:text-foreground"
            >
              Editar
            </button>
            <button
              onClick={() => onDelete(content.id)}
              className="text-xs text-muted-foreground hover:text-destructive"
            >
              Excluir
            </button>
          </>
        )}
      </div>
    </div>
  )
}

export default ContentCard
