import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import ActivityFeed from './ActivityFeed'
import type { ActivityItem } from '../types'

describe('ActivityFeed', () => {
  it('renders each activity item with its description', () => {
    const activity: ActivityItem[] = [
      { type: 'CLASSROOM_CREATED', referenceId: 'c-1', description: 'Turma "Matemática" criada', occurredAt: '2026-01-10T10:00:00' },
      { type: 'MEMBER_JOINED', referenceId: 'm-1', description: 'Novo membro (ALUNO) ingressou na organização', occurredAt: '2026-01-12T10:00:00' },
    ]

    render(<ActivityFeed activity={activity} />)

    expect(screen.getByText('Turma "Matemática" criada')).toBeTruthy()
    expect(screen.getByText('Novo membro (ALUNO) ingressou na organização')).toBeTruthy()
  })

  it('shows an empty state when there is no activity in the period', () => {
    render(<ActivityFeed activity={[]} />)

    expect(screen.getByText(/nenhuma atividade no período/i)).toBeTruthy()
  })
})
