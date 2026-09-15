import { cloneElement, type ReactElement } from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import DashboardCharts from './DashboardCharts'
import type { AdminDashboardData } from '../types'

// jsdom não mede layout: sem tamanho fixo o ResponsiveContainer não desenha os eixos.
vi.mock('recharts', async (importOriginal) => {
  const recharts = await importOriginal<typeof import('recharts')>()
  return {
    ...recharts,
    ResponsiveContainer: ({ children }: { children: ReactElement }) =>
      cloneElement(children, { width: 600, height: 220 } as Record<string, number>),
  }
})

const dashboard: AdminDashboardData = {
  from: '2026-01-01',
  to: '2026-01-31',
  classroomsByStatus: {},
  membersByRole: { ADMIN_ORG: 1, ALUNO: 10 },
  tasksCreated: 0,
  tasksEvaluated: 0,
  averageDeliveryRate: 0,
  activity: [],
}

describe('DashboardCharts', () => {
  it('labels the members chart axis with readable role names', () => {
    render(<DashboardCharts dashboard={dashboard} />)

    expect(screen.getByText('Administrador')).toBeTruthy()
    expect(screen.getByText('Aluno')).toBeTruthy()
    expect(screen.queryByText('ADMIN_ORG')).toBeNull()
  })
})
