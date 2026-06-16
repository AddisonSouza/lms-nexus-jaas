import { Pencil, Trash2, Paperclip, Link as LinkIcon } from 'lucide-react'
import type { Announcement } from '../types'

interface Props {
  announcement: Announcement
  canManage: boolean
  onEdit: () => void
  onDelete: () => void
}

function AnnouncementCard({ announcement, canManage, onEdit, onDelete }: Props) {
  return (
    <div className="rounded-lg border p-4 space-y-2">
      <div className="flex items-start justify-between gap-2">
        <p className="whitespace-pre-wrap text-sm">{announcement.content}</p>
        {canManage && (
          <div className="flex shrink-0 gap-1">
            <button onClick={onEdit} className="text-muted-foreground hover:text-foreground" title="Editar">
              <Pencil className="h-4 w-4" />
            </button>
            <button onClick={onDelete} className="text-muted-foreground hover:text-destructive" title="Excluir">
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        )}
      </div>

      {announcement.attachments.length > 0 && (
        <ul className="space-y-1 text-xs">
          {announcement.attachments.map((att) => (
            <li key={att.id} className="flex items-center gap-1 text-muted-foreground">
              {att.externalUrl ? (
                <>
                  <LinkIcon className="h-3 w-3" />
                  <a href={att.externalUrl} target="_blank" rel="noreferrer" className="underline">
                    {att.linkTitle || att.externalUrl}
                  </a>
                </>
              ) : (
                <>
                  <Paperclip className="h-3 w-3" />
                  <span>{att.originalName}</span>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      <p className="text-xs text-muted-foreground">
        {new Date(announcement.createdAt).toLocaleString('pt-BR')}
      </p>
    </div>
  )
}

export default AnnouncementCard
