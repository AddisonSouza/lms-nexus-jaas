import { CheckCircle2, Clock } from 'lucide-react'

interface Props {
  submittedCount: number
  pendingCount: number
}

function SubmissionStatusSummary({ submittedCount, pendingCount }: Props) {
  return (
    <div className="grid grid-cols-2 gap-4">
      <div className="flex items-center gap-3 rounded-lg border p-4">
        <CheckCircle2 className="h-5 w-5 text-muted-foreground" />
        <div>
          <p className="text-xs text-muted-foreground">Tarefas entregues</p>
          <p className="text-lg font-semibold">{submittedCount}</p>
        </div>
      </div>
      <div className="flex items-center gap-3 rounded-lg border p-4">
        <Clock className="h-5 w-5 text-muted-foreground" />
        <div>
          <p className="text-xs text-muted-foreground">Tarefas pendentes</p>
          <p className="text-lg font-semibold">{pendingCount}</p>
        </div>
      </div>
    </div>
  )
}

export default SubmissionStatusSummary
