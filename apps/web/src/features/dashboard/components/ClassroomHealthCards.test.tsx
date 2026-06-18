import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import ClassroomHealthCards from './ClassroomHealthCards'
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

describe('ClassroomHealthCards', () => {
  it('renders delivery rate and average grade for each classroom', () => {
    render(
      <ClassroomHealthCards
        classrooms={[classroom({ classroomName: 'Turma A', deliveryRate: 0.8, averageGrade: 7.5 })]}
      />,
    )

    expect(screen.getByText('Turma A')).toBeTruthy()
    expect(screen.getByText('80%')).toBeTruthy()
    expect(screen.getByText('7.5')).toBeTruthy()
  })

  it('shows "Sem notas ainda" when averageGrade is null', () => {
    render(<ClassroomHealthCards classrooms={[classroom({ averageGrade: null })]} />)

    expect(screen.getByText('Sem notas ainda')).toBeTruthy()
  })

  it('shows an empty state when there are no classrooms', () => {
    render(<ClassroomHealthCards classrooms={[]} />)

    expect(screen.getByText(/nenhuma turma cadastrada/i)).toBeTruthy()
  })
})
