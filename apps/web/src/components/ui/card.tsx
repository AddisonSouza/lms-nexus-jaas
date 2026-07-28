import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@features/lib/utils"

const cardVariants = cva(
  "flex flex-col gap-[var(--space-2)] rounded-[calc(var(--radius-lg)*1.15)] bg-surface p-[var(--space-3)] text-foreground",
  {
    variants: {
      elevation: {
        none: "",
        sm: "shadow-sm",
        md: "shadow-md",
        lg: "shadow-lg",
      },
    },
    defaultVariants: {
      elevation: "none",
    },
  }
)

interface CardProps
  extends React.ComponentProps<"div">,
    VariantProps<typeof cardVariants> {}

function Card({ className, elevation, ...props }: CardProps) {
  return (
    <div
      data-slot="card"
      className={cn(cardVariants({ elevation }), className)}
      {...props}
    />
  )
}

function CardKicker({ className, ...props }: React.ComponentProps<"span">) {
  return (
    <span
      data-slot="card-kicker"
      className={cn(
        "text-[10px] tracking-[0.1em] text-accent uppercase",
        className
      )}
      {...props}
    />
  )
}

function CardTitle({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="card-title"
      className={cn("font-heading text-[17px] leading-tight", className)}
      {...props}
    />
  )
}

function CardBody({ className, ...props }: React.ComponentProps<"p">) {
  return (
    <p
      data-slot="card-body"
      className={cn("m-0 flex-1 text-[13px] opacity-80", className)}
      {...props}
    />
  )
}

function CardMeta({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="card-meta"
      className={cn(
        "flex items-center gap-1.5 text-[11px] text-muted-foreground",
        className
      )}
      {...props}
    />
  )
}

export { Card, CardKicker, CardTitle, CardBody, CardMeta, cardVariants }
