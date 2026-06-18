import { ClipboardList } from 'lucide-react'

interface Props {
  count: number
}

function PendingEvaluationsBadge({ count }: Props) {
  return (
    <div className="flex items-center gap-3 rounded-lg border p-4">
      <ClipboardList className="h-5 w-5 text-muted-foreground" />
      <div>
        <p className="text-xs text-muted-foreground">Submissões pendentes de avaliação</p>
        <p className="text-lg font-semibold">{count}</p>
      </div>
    </div>
  )
}

export default PendingEvaluationsBadge
