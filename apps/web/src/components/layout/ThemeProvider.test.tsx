import { act, render } from '@testing-library/react'
import { describe, it, expect, beforeEach } from 'vitest'
import ThemeProvider from './ThemeProvider'
import { useThemeStore } from '@store/themeStore'

beforeEach(() => {
  localStorage.clear()
  document.documentElement.classList.remove('dark')
  useThemeStore.setState({ theme: 'light' })
})

describe('ThemeProvider', () => {
  it('does not apply the dark class by default', () => {
    render(
      <ThemeProvider>
        <div>conteúdo</div>
      </ThemeProvider>,
    )

    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('applies and removes the dark class when the theme is toggled, and persists the choice', () => {
    render(
      <ThemeProvider>
        <div>conteúdo</div>
      </ThemeProvider>,
    )

    act(() => useThemeStore.getState().toggleTheme())
    expect(document.documentElement.classList.contains('dark')).toBe(true)
    expect(JSON.parse(localStorage.getItem('nexus-theme') ?? '{}').state.theme).toBe('dark')

    act(() => useThemeStore.getState().toggleTheme())
    expect(document.documentElement.classList.contains('dark')).toBe(false)
    expect(JSON.parse(localStorage.getItem('nexus-theme') ?? '{}').state.theme).toBe('light')
  })

  it('restores a previously persisted dark preference on remount', () => {
    localStorage.setItem('nexus-theme', JSON.stringify({ state: { theme: 'dark' }, version: 0 }))
    useThemeStore.persist.rehydrate()

    render(
      <ThemeProvider>
        <div>conteúdo</div>
      </ThemeProvider>,
    )

    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})
