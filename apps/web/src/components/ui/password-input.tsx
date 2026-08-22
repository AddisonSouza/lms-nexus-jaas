import * as React from "react"
import { Eye, EyeOff } from "lucide-react"

import { cn } from "@features/lib/utils"
import { Input } from "@components/ui/input"

type PasswordInputProps = Omit<React.ComponentProps<"input">, "type">

const PasswordInput = React.forwardRef<HTMLInputElement, PasswordInputProps>(
  ({ className, ...props }, ref) => {
    const [visible, setVisible] = React.useState(false)
    const Icon = visible ? EyeOff : Eye

    return (
      <div className="relative">
        <Input
          {...props}
          ref={ref}
          type={visible ? "text" : "password"}
          className={cn("pr-10", className)}
        />
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? "Ocultar senha" : "Mostrar senha"}
          aria-pressed={visible}
          className="absolute top-1/2 right-3 -translate-y-1/2 text-muted-foreground transition-colors outline-none hover:text-foreground focus-visible:text-accent"
        >
          <Icon className="h-4 w-4" />
        </button>
      </div>
    )
  }
)
PasswordInput.displayName = "PasswordInput"

export { PasswordInput }
