import * as React from "react"

import { cn } from "@features/lib/utils"

const Textarea = React.forwardRef<HTMLTextAreaElement, React.ComponentProps<"textarea">>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      data-slot="textarea"
      className={cn(
        "flex min-h-[90px] w-full resize-y rounded-[var(--radius-md)] border border-border bg-surface px-3.5 py-2 text-sm text-foreground caret-accent outline-none transition-colors placeholder:text-muted-foreground hover:border-neutral-500 focus-visible:border-accent disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      {...props}
    />
  )
)
Textarea.displayName = "Textarea"

export { Textarea }
