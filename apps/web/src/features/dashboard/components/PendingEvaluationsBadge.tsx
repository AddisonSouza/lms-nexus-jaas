import { ClipboardList } from 'lucide-react'
import { Card, CardKicker } from '@components/ui/card'

interface Props {
  count: number
}

function PendingEvaluationsBadge({ count }: Props) {
  return (
    <Card elevation="sm" className="w-fit flex-row items-center gap-3">
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-accent text-accent-foreground">
        <ClipboardList className="h-5 w-5" />
      </div>
      <div>
        <CardKicker>Submissões pendentes de avaliação</CardKicker>
        <div className="font-heading text-3xl leading-none">{count}</div>
      </div>
    </Card>
  )
}

export default PendingEvaluationsBadge
