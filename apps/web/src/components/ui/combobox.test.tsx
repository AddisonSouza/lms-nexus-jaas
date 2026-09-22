import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeAll } from 'vitest'
import { Combobox, ComboboxContent, ComboboxInput, ComboboxItem, ComboboxList } from './combobox'

// O jsdom não implementa nada disso, e o Positioner do base-ui usa os três.
beforeAll(() => {
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    }
  )
  Element.prototype.scrollIntoView = vi.fn()
  if (!Element.prototype.hasPointerCapture) {
    Element.prototype.hasPointerCapture = () => false
  }
})

const PEOPLE = ['Ana Beatriz', 'Bruno Carvalho']

function Subject({ onValueChange = vi.fn() }: { onValueChange?: (value: unknown) => void }) {
  return (
    <Combobox items={PEOPLE} defaultOpen onValueChange={onValueChange}>
      <ComboboxInput placeholder="Buscar pessoa" />
      <ComboboxContent>
        <ComboboxList>
          {PEOPLE.map((person) => (
            <ComboboxItem key={person} value={person}>
              {person}
            </ComboboxItem>
          ))}
        </ComboboxList>
      </ComboboxContent>
    </Combobox>
  )
}

describe('Combobox', () => {
  it('does not filter on the client, so the server result stays visible', async () => {
    const user = userEvent.setup()
    render(<Subject />)

    await user.type(screen.getByPlaceholderText('Buscar pessoa'), 'zzz')

    // Nenhum item casa com "zzz". Com o filtro do base-ui ligado os dois
    // sumiriam — e é justamente isso que `filter={null}` impede, porque quem
    // filtra é a API.
    expect(screen.getByText('Ana Beatriz')).toBeTruthy()
    expect(screen.getByText('Bruno Carvalho')).toBeTruthy()
  })

  it('reports the picked item through onValueChange', async () => {
    const user = userEvent.setup()
    const onValueChange = vi.fn()
    render(<Subject onValueChange={onValueChange} />)

    await user.click(screen.getByText('Bruno Carvalho'))

    expect(onValueChange).toHaveBeenCalled()
    expect(onValueChange.mock.calls[0][0]).toBe('Bruno Carvalho')
  })
})
