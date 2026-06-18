import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import PendingEvaluationsBadge from './PendingEvaluationsBadge'

describe('PendingEvaluationsBadge', () => {
  it('renders the pending evaluations count', () => {
    render(<PendingEvaluationsBadge count={5} />)

    expect(screen.getByText('5')).toBeTruthy()
  })
})
