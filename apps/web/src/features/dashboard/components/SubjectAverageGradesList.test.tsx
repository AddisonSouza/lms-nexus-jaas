import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import SubjectAverageGradesList from './SubjectAverageGradesList'

describe('SubjectAverageGradesList', () => {
  it('renders the average grade per subject', () => {
    render(
      <SubjectAverageGradesList subjects={[{ subjectId: 'sub-1', subjectName: 'Disciplina A', averageGrade: 8.5 }]} />,
    )

    expect(screen.getByText('Disciplina A')).toBeTruthy()
    expect(screen.getByText('8.5')).toBeTruthy()
  })

  it('shows an empty state when there are no averages yet', () => {
    render(<SubjectAverageGradesList subjects={[]} />)

    expect(screen.getByText(/nenhuma média disponível/i)).toBeTruthy()
  })
})
