import * as React from "react"

import { cn } from "@features/lib/utils"
import { Input } from "@components/ui/input"

/** Hora sugerida quando o usuário escolhe a data e ainda não informou a hora. */
const SUGGESTED_TIME = "23:59"

interface DateTimeParts {
  date: string
  time: string
}

function splitValue(value: string): DateTimeParts {
  const [date = "", time = ""] = value.split("T")
  return { date, time: time.slice(0, 5) }
}

function joinParts({ date, time }: DateTimeParts): string {
  return date && time ? `${date}T${time}` : ""
}

interface DateTimeFieldProps {
  id?: string
  /** Data e hora no formato ISO local `YYYY-MM-DDTHH:mm`, ou `''` quando incompleto. */
  value: string
  onChange: (value: string) => void
  onBlur?: () => void
  disabled?: boolean
  className?: string
  dateLabel?: string
  timeLabel?: string
}

/**
 * Campo de data e hora em dois inputs nativos — `dd/mm/aaaa` e `HH:mm`, sem
 * segundos. Emite `''` enquanto data ou hora estiverem vazias.
 */
const DateTimeField = React.forwardRef<HTMLInputElement, DateTimeFieldProps>(
  (
    {
      id,
      value,
      onChange,
      onBlur,
      disabled,
      className,
      dateLabel = "Data",
      timeLabel = "Hora",
    },
    ref
  ) => {
    const [parts, setParts] = React.useState<DateTimeParts>(() => splitValue(value))

    // Mantém os dois inputs em sincronia quando o valor muda de fora (reset do form).
    React.useEffect(() => {
      setParts((prev) => (joinParts(prev) === value ? prev : splitValue(value)))
    }, [value])

    function update(next: DateTimeParts) {
      setParts(next)
      onChange(joinParts(next))
    }

    function handleDateChange(event: React.ChangeEvent<HTMLInputElement>) {
      const date = event.target.value
      update({ date, time: parts.time || (date ? SUGGESTED_TIME : "") })
    }

    function handleTimeChange(event: React.ChangeEvent<HTMLInputElement>) {
      update({ ...parts, time: event.target.value.slice(0, 5) })
    }

    return (
      <div className={cn("flex gap-2", className)}>
        <Input
          id={id}
          ref={ref}
          type="date"
          aria-label={dateLabel}
          value={parts.date}
          onChange={handleDateChange}
          onBlur={onBlur}
          disabled={disabled}
          className="min-w-0 flex-1"
        />
        <Input
          type="time"
          aria-label={timeLabel}
          value={parts.time}
          onChange={handleTimeChange}
          onBlur={onBlur}
          disabled={disabled}
          className="w-28 shrink-0"
        />
      </div>
    )
  }
)
DateTimeField.displayName = "DateTimeField"

export { DateTimeField }
