import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import StudentsWithoutSubmissionList from './StudentsWithoutSubmissionList'

describe('StudentsWithoutSubmissionList', () => {
  it('renders the list of students without submission', () => {
    render(
      <StudentsWithoutSubmissionList
        students={[{ studentId: 's-1', studentName: 'Aluno 1' }]}
      />,
    )

    expect(screen.getByText('Aluno 1')).toBeTruthy()
  })

  it('shows an empty state when every student submitted', () => {
    render(<StudentsWithoutSubmissionList students={[]} />)

    expect(screen.getByText(/todos os alunos entregaram/i)).toBeTruthy()
  })
})
