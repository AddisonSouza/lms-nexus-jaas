import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import LastTaskGradeChart from './LastTaskGradeChart'

describe('LastTaskGradeChart', () => {
  it('renders a chart when there are grades', () => {
    const { container } = render(<LastTaskGradeChart grades={[8.5, 7.0, 9.2]} />)

    expect(container.querySelector('.recharts-responsive-container')).toBeTruthy()
  })

  it('shows "Sem notas ainda" when the grade list is empty', () => {
    render(<LastTaskGradeChart grades={[]} />)

    expect(screen.getByText('Sem notas ainda.')).toBeTruthy()
  })
})
