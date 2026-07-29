import * as React from "react"

import { cn } from "@features/lib/utils"

interface SegmentedOption {
  value: string
  label: React.ReactNode
}

interface SegmentedProps {
  value: string
  onValueChange: (value: string) => void
  options: SegmentedOption[]
  className?: string
}

function Segmented({ value, onValueChange, options, className }: SegmentedProps) {
  return (
    <div
      data-slot="segmented"
      role="radiogroup"
      className={cn(
        "inline-flex overflow-hidden rounded-full border border-border",
        className
      )}
    >
      {options.map((option, index) => {
        const active = option.value === value
        return (
          <button
            key={option.value}
            type="button"
            role="radio"
            aria-checked={active}
            onClick={() => onValueChange(option.value)}
            className={cn(
              "px-3 py-1.5 text-[13px] whitespace-nowrap transition-colors",
              index > 0 && "border-l border-border",
              active ? "bg-accent text-accent-foreground" : "hover:bg-muted"
            )}
          >
            {option.label}
          </button>
        )
      })}
    </div>
  )
}

export { Segmented }
export type { SegmentedOption }
