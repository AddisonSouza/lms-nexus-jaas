import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import SubmissionStatusSummary from './SubmissionStatusSummary'

describe('SubmissionStatusSummary', () => {
  it('renders the submitted and pending counts', () => {
    render(<SubmissionStatusSummary submittedCount={3} pendingCount={2} />)

    expect(screen.getByText('3')).toBeTruthy()
    expect(screen.getByText('2')).toBeTruthy()
  })
})
