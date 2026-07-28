import { CheckCircle2, Clock } from 'lucide-react'
import { Card, CardKicker } from '@components/ui/card'

interface Props {
  submittedCount: number
  pendingCount: number
}

function SubmissionStatusSummary({ submittedCount, pendingCount }: Props) {
  return (
    <div className="grid grid-cols-2 gap-3">
      <Card elevation="sm" className="flex-row items-center gap-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-accent-2-100 text-accent-2-800">
          <CheckCircle2 className="h-5 w-5" />
        </div>
        <div>
          <CardKicker>Tarefas entregues</CardKicker>
          <div className="font-heading text-3xl leading-none">{submittedCount}</div>
        </div>
      </Card>
      <Card elevation="sm" className="flex-row items-center gap-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-neutral-100 text-neutral-800">
          <Clock className="h-5 w-5" />
        </div>
        <div>
          <CardKicker>Tarefas pendentes</CardKicker>
          <div className="font-heading text-3xl leading-none">{pendingCount}</div>
        </div>
      </Card>
    </div>
  )
}

export default SubmissionStatusSummary
