import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import AtRiskStudentsList from './AtRiskStudentsList'
import type { ClassroomHealth } from '../types'

function classroom(overrides: Partial<ClassroomHealth> = {}): ClassroomHealth {
  return {
    classroomId: 'c-1',
    classroomName: 'Turma A',
    status: 'ACTIVE',
    deliveryRate: 0,
    averageGrade: null,
    atRiskStudents: [],
    ...overrides,
  }
}

describe('AtRiskStudentsList', () => {
  it('renders at-risk students grouped by classroom', () => {
    render(
      <AtRiskStudentsList
        classrooms={[
          classroom({
            classroomName: 'Turma A',
            atRiskStudents: [{ studentId: 's-1', studentName: 'Aluno 1', pendingCount: 3 }],
          }),
        ]}
      />,
    )

    expect(screen.getByText('Turma A')).toBeTruthy()
    expect(screen.getByText('Aluno 1')).toBeTruthy()
    expect(screen.getByText('3 pendência(s)')).toBeTruthy()
  })

  it('shows an empty state when no classroom has at-risk students', () => {
    render(<AtRiskStudentsList classrooms={[classroom()]} />)

    expect(screen.getByText(/nenhum aluno com pend/i)).toBeTruthy()
  })
})
