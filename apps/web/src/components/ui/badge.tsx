import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@features/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] tracking-wide whitespace-nowrap",
  {
    variants: {
      variant: {
        accent: "bg-accent-100 text-accent-800",
        "accent-2": "bg-accent-2-100 text-accent-2-800",
        neutral: "bg-neutral-100 text-neutral-800",
        outline: "border border-accent text-accent",
      },
    },
    defaultVariants: {
      variant: "neutral",
    },
  }
)

interface BadgeProps
  extends React.ComponentProps<"span">,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <span
      data-slot="badge"
      className={cn(badgeVariants({ variant }), className)}
      {...props}
    />
  )
}

export { Badge, badgeVariants }
