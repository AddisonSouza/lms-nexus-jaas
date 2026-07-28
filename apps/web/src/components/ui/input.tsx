import * as React from "react"

import { cn } from "@features/lib/utils"

const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input">>(
  ({ className, type, ...props }, ref) => (
    <input
      type={type}
      ref={ref}
      data-slot="input"
      className={cn(
        "flex h-9 w-full min-w-0 rounded-full border border-border bg-surface px-3.5 text-sm text-foreground caret-accent outline-none transition-colors placeholder:text-muted-foreground hover:border-neutral-500 focus-visible:border-accent disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      {...props}
    />
  )
)
Input.displayName = "Input"

export { Input }
