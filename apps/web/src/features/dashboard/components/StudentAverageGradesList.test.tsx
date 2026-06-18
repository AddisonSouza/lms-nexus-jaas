import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import StudentAverageGradesList from './StudentAverageGradesList'

describe('StudentAverageGradesList', () => {
  it('renders the average grade per student', () => {
    render(
      <StudentAverageGradesList
        students={[{ studentId: 's-1', studentName: 'Aluno 1', averageGrade: 8.5 }]}
      />,
    )

    expect(screen.getByText('Aluno 1')).toBeTruthy()
    expect(screen.getByText('8.5')).toBeTruthy()
  })

  it('shows an empty state when there are no evaluated submissions', () => {
    render(<StudentAverageGradesList students={[]} />)

    expect(screen.getByText(/nenhuma submissão avaliada/i)).toBeTruthy()
  })
})
