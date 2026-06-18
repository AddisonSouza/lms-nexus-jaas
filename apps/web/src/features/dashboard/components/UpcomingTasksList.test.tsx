import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import UpcomingTasksList from './UpcomingTasksList'

describe('UpcomingTasksList', () => {
  it('renders upcoming pending tasks ordered by urgency', () => {
    render(
      <UpcomingTasksList
        tasks={[
          { taskId: 't-1', title: 'Tarefa 1', subjectName: 'Disciplina A', deadline: '2026-07-01T00:00:00' },
          { taskId: 't-2', title: 'Tarefa 2', subjectName: 'Disciplina B', deadline: '2026-07-10T00:00:00' },
        ]}
      />,
    )

    expect(screen.getByText('Tarefa 1')).toBeTruthy()
    expect(screen.getByText('Tarefa 2')).toBeTruthy()
  })

  it('shows an empty state when there are no pending tasks', () => {
    render(<UpcomingTasksList tasks={[]} />)

    expect(screen.getByText(/nenhuma tarefa pendente/i)).toBeTruthy()
  })
})
