import { Combobox as ComboboxPrimitive } from "@base-ui/react/combobox"

import { cn } from "@features/lib/utils"

/**
 * O filtro embutido do base-ui fica desligado por padrão (`filter={null}`).
 * As listas deste app chegam filtradas e paginadas do servidor: filtrar de novo
 * no cliente esconderia itens que a própria busca acabou de devolver.
 */
function Combobox<Value>({ filter = null, ...props }: ComboboxPrimitive.Root.Props<Value>) {
  return <ComboboxPrimitive.Root data-slot="combobox" filter={filter} {...props} />
}

function ComboboxInput({ className, ...props }: ComboboxPrimitive.Input.Props) {
  return (
    <ComboboxPrimitive.Input
      data-slot="combobox-input"
      className={cn(
        "flex h-9 w-full min-w-0 rounded-full border border-border bg-surface px-3.5 text-sm text-foreground caret-accent outline-none transition-colors placeholder:text-muted-foreground hover:border-neutral-500 focus-visible:border-accent disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      {...props}
    />
  )
}

function ComboboxContent({
  className,
  children,
  sideOffset = 6,
  align = "start",
  ...props
}: ComboboxPrimitive.Popup.Props &
  Pick<ComboboxPrimitive.Positioner.Props, "sideOffset" | "align">) {
  return (
    <ComboboxPrimitive.Portal>
      <ComboboxPrimitive.Positioner
        data-slot="combobox-positioner"
        sideOffset={sideOffset}
        align={align}
        className="z-50 outline-none"
      >
        <ComboboxPrimitive.Popup
          data-slot="combobox-content"
          className={cn(
            "max-h-64 w-[var(--anchor-width)] overflow-y-auto rounded-[calc(var(--radius-lg)*1.15)] bg-popover p-1 text-sm text-popover-foreground shadow-lg outline-none data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
            className
          )}
          {...props}
        >
          {children}
        </ComboboxPrimitive.Popup>
      </ComboboxPrimitive.Positioner>
    </ComboboxPrimitive.Portal>
  )
}

function ComboboxList({ className, ...props }: ComboboxPrimitive.List.Props) {
  return (
    <ComboboxPrimitive.List
      data-slot="combobox-list"
      className={cn("flex flex-col gap-0.5", className)}
      {...props}
    />
  )
}

function ComboboxItem({ className, ...props }: ComboboxPrimitive.Item.Props) {
  return (
    <ComboboxPrimitive.Item
      data-slot="combobox-item"
      className={cn(
        "flex cursor-default select-none flex-col items-start rounded-lg px-3 py-2 outline-none data-highlighted:bg-accent/10 data-disabled:cursor-not-allowed data-disabled:opacity-60",
        className
      )}
      {...props}
    />
  )
}

function ComboboxEmpty({ className, ...props }: ComboboxPrimitive.Empty.Props) {
  return (
    <ComboboxPrimitive.Empty
      data-slot="combobox-empty"
      className={cn("px-3 py-6 text-center text-sm text-muted-foreground", className)}
      {...props}
    />
  )
}

export { Combobox, ComboboxInput, ComboboxContent, ComboboxList, ComboboxItem, ComboboxEmpty }
