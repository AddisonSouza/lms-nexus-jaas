import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import RecentGradesList from './RecentGradesList'

describe('RecentGradesList', () => {
  it('renders the recent grades and feedback', () => {
    render(
      <RecentGradesList
        grades={[
          { taskId: 't-1', title: 'Tarefa 1', subjectName: 'Disciplina A', grade: 8.5, feedback: 'Bom trabalho' },
        ]}
      />,
    )

    expect(screen.getByText('Tarefa 1')).toBeTruthy()
    expect(screen.getByText('8.5')).toBeTruthy()
    expect(screen.getByText('Bom trabalho')).toBeTruthy()
  })

  it('shows an empty state when there are no grades yet', () => {
    render(<RecentGradesList grades={[]} />)

    expect(screen.getByText(/nenhuma nota recebida/i)).toBeTruthy()
  })
})
