import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import MetricsCards from './MetricsCards'
import type { AdminDashboardData } from '../types'

function dashboard(overrides: Partial<AdminDashboardData> = {}): AdminDashboardData {
  return {
    from: '2026-01-01',
    to: '2026-01-31',
    classroomsByStatus: {},
    membersByRole: {},
    tasksCreated: 0,
    tasksEvaluated: 0,
    averageDeliveryRate: 0,
    activity: [],
    ...overrides,
  }
}

describe('MetricsCards', () => {
  it('renders classroom, member, task and delivery rate metrics', () => {
    render(
      <MetricsCards
        dashboard={dashboard({
          classroomsByStatus: { ACTIVE: 3, ARCHIVED: 1 },
          membersByRole: { ALUNO: 10, PROFESSOR: 2 },
          tasksCreated: 5,
          tasksEvaluated: 3,
          averageDeliveryRate: 0.75,
        })}
      />,
    )

    expect(screen.getByText('3 / 1')).toBeTruthy()
    expect(screen.getByText('5 / 3')).toBeTruthy()
    expect(screen.getByText('75%')).toBeTruthy()
    expect(screen.getByText(/ALUNO: 10/)).toBeTruthy()
  })

  it('renders zeroed metrics for an empty period without error', () => {
    render(<MetricsCards dashboard={dashboard()} />)

    expect(screen.getAllByText('0 / 0')).toHaveLength(2)
    expect(screen.getByText('0%')).toBeTruthy()
    expect(screen.getByText('Sem membros no período')).toBeTruthy()
  })
})
