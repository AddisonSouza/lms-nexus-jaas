import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { periodSchema, type PeriodFormValues } from '../schemas/periodSchema'
import type { DashboardPeriod } from '../types'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface Props {
  value: DashboardPeriod
  onChange: (period: DashboardPeriod) => void
}

function toIsoDate(date: Date) {
  return date.toISOString().slice(0, 10)
}

function shortcutRange(days: number): DashboardPeriod {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - days)
  return { from: toIsoDate(from), to: toIsoDate(to) }
}

function PeriodSelector({ value, onChange }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<PeriodFormValues>({
    resolver: zodResolver(periodSchema),
    defaultValues: value,
  })

  useEffect(() => {
    reset(value)
  }, [value, reset])

  function onSubmit(data: PeriodFormValues) {
    onChange(data)
  }

  return (
    <div className="flex flex-wrap items-end gap-3">
      <div className="flex gap-2">
        <Button type="button" variant="secondary" size="sm" onClick={() => onChange(shortcutRange(7))}>
          7 dias
        </Button>
        <Button type="button" variant="secondary" size="sm" onClick={() => onChange(shortcutRange(30))}>
          30 dias
        </Button>
        <Button type="button" variant="secondary" size="sm" onClick={() => onChange(shortcutRange(90))}>
          90 dias
        </Button>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex items-end gap-2">
        <div className="space-y-1">
          <label htmlFor="period-from" className="text-xs text-muted-foreground">De</label>
          <Input id="period-from" type="date" {...register('from')} className="h-8" />
        </div>
        <div className="space-y-1">
          <label htmlFor="period-to" className="text-xs text-muted-foreground">Até</label>
          <Input id="period-to" type="date" {...register('to')} className="h-8" />
        </div>
        <Button type="submit" size="sm">Aplicar</Button>
      </form>
      {(errors.from || errors.to) && (
        <p className="w-full text-xs text-destructive">{errors.from?.message ?? errors.to?.message}</p>
      )}
    </div>
  )
}

export default PeriodSelector
