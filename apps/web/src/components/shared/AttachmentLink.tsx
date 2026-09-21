import { useState } from 'react'
import { Download, Loader2, Paperclip } from 'lucide-react'
import { downloadFile } from '@lib/download'
import { apiErrorMessage } from '@lib/api-error'

interface Props {
  fileKey: string
  originalName: string
  sizeBytes?: number
  className?: string
}

/** 1,2 MB em vez de 1234567 — o número cru não diz nada a quem vai baixar. */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB']
  let value = bytes / 1024
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  return `${value.toFixed(1).replace('.', ',')} ${units[unit]}`
}

/**
 * Anexo baixável. O download passa pelo `api` para levar o JWT, então isto é um
 * `<button>`, não um `<a href>`: o link direto abria uma aba em 401.
 */
function AttachmentLink({ fileKey, originalName, sizeBytes, className = '' }: Props) {
  const [isDownloading, setIsDownloading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleDownload() {
    setIsDownloading(true)
    setError(null)
    try {
      await downloadFile(fileKey, originalName)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <span className={`inline-flex flex-col gap-0.5 ${className}`}>
      <button
        type="button"
        onClick={handleDownload}
        disabled={isDownloading}
        // O texto visível é só o nome do arquivo; o rótulo diz que o clique baixa.
        aria-label={`Baixar ${originalName}`}
        title={`Baixar ${originalName}`}
        className="group inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground disabled:opacity-60"
      >
        {isDownloading ? (
          <Loader2 className="h-4 w-4 shrink-0 animate-spin" />
        ) : (
          <Paperclip className="h-4 w-4 shrink-0" />
        )}
        <span className="truncate underline-offset-2 group-hover:underline">{originalName}</span>
        {sizeBytes != null && (
          <span className="shrink-0 text-xs text-muted-foreground">({formatFileSize(sizeBytes)})</span>
        )}
        {!isDownloading && (
          <Download className="h-3.5 w-3.5 shrink-0 opacity-0 transition-opacity group-hover:opacity-100" />
        )}
      </button>
      {error && (
        <span role="alert" className="text-xs text-destructive">
          {error}
        </span>
      )}
    </span>
  )
}

export default AttachmentLink
