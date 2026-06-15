import { FileText, Video, Link2, Archive, ExternalLink, Download } from 'lucide-react'
import type { SubjectContent, ContentType } from '../types'

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

interface Props {
  content: SubjectContent
  canManage: boolean
  onEdit: (content: SubjectContent) => void
  onDelete: (contentId: string) => void
  apiBaseUrl: string
}

function ContentCard({ content, canManage, onEdit, onDelete, apiBaseUrl }: Props) {
  const Icon = ICONS[content.contentType]

  const href = content.externalUrl ?? (content.fileKey ? `${apiBaseUrl}/files/${content.fileKey}` : undefined)

  return (
    <div className="flex items-start gap-3 rounded border bg-card px-4 py-3">
      <div className="mt-0.5 shrink-0 text-muted-foreground">
        <Icon className="h-4 w-4" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium">{content.title}</p>
        {content.description && (
          <p className="mt-0.5 text-xs text-muted-foreground line-clamp-2">{content.description}</p>
        )}
        <span className="mt-1 inline-block rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground">
          {LABELS[content.contentType]}
        </span>
      </div>

      <div className="flex shrink-0 items-center gap-2">
        {href && (
          <a
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            className="text-muted-foreground hover:text-foreground"
            title={content.fileKey ? 'Download' : 'Abrir link'}
          >
            {content.fileKey ? <Download className="h-4 w-4" /> : <ExternalLink className="h-4 w-4" />}
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
