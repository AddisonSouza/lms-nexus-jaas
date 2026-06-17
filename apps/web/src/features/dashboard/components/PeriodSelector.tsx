import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { periodSchema, type PeriodFormValues } from '../schemas/periodSchema'
import type { DashboardPeriod } from '../types'

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
        <button
          type="button"
          onClick={() => onChange(shortcutRange(7))}
          className="rounded border px-3 py-1.5 text-sm hover:bg-muted"
        >
          7 dias
        </button>
        <button
          type="button"
          onClick={() => onChange(shortcutRange(30))}
          className="rounded border px-3 py-1.5 text-sm hover:bg-muted"
        >
          30 dias
        </button>
        <button
          type="button"
          onClick={() => onChange(shortcutRange(90))}
          className="rounded border px-3 py-1.5 text-sm hover:bg-muted"
        >
          90 dias
        </button>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex items-end gap-2">
        <div className="space-y-1">
          <label htmlFor="period-from" className="text-xs text-muted-foreground">De</label>
          <input id="period-from" type="date" {...register('from')} className="rounded border px-2 py-1.5 text-sm" />
        </div>
        <div className="space-y-1">
          <label htmlFor="period-to" className="text-xs text-muted-foreground">Até</label>
          <input id="period-to" type="date" {...register('to')} className="rounded border px-2 py-1.5 text-sm" />
        </div>
        <button type="submit" className="rounded bg-primary px-3 py-1.5 text-sm text-primary-foreground">
          Aplicar
        </button>
      </form>
      {(errors.from || errors.to) && (
        <p className="w-full text-xs text-destructive">{errors.from?.message ?? errors.to?.message}</p>
      )}
    </div>
  )
}

export default PeriodSelector
