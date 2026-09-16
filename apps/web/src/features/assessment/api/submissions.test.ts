import { describe, it, expect, vi, beforeEach } from 'vitest'
import { listStudentGrades, updateSubmission } from './submissions'
import api from '@lib/axios'

vi.mock('@lib/axios', () => ({
  default: { get: vi.fn(), put: vi.fn() },
  API_BASE_URL: '',
}))

/**
 * Payload real de `GET /tasks/my-grades`. `TaskWithGradeResponse` não tem
 * `updatedAt` — o schema não pode exigi-lo, senão a lista do aluno fica vazia.
 */
const myGradesPayload = [
  {
    id: 'task-1',
    subjectId: 'sub-1',
    organizationId: 'org-1',
    createdBy: 'user-1',
    title: 'Trabalho de Álgebra',
    description: 'Resolva os exercícios',
    deadline: '2026-12-31T23:59:00',
    maxScore: 10,
    status: 'PUBLISHED',
    attachments: [],
    createdAt: '2026-09-01T10:00:00',
    submission: {
      id: 'sub-id-1',
      status: 'EVALUATED',
      grade: 8.5,
      feedback: 'Bom trabalho',
      submittedAt: '2026-09-10T12:00:00',
      lateSubmission: false,
    },
  },
  {
    id: 'task-2',
    subjectId: 'sub-1',
    organizationId: 'org-1',
    createdBy: 'user-1',
    title: 'Lista de Geometria',
    description: 'Entregue em PDF',
    deadline: '2026-11-30T23:59:00',
    maxScore: null,
    status: 'PUBLISHED',
    attachments: [],
    createdAt: '2026-09-02T10:00:00',
    submission: null,
  },
]

beforeEach(() => vi.clearAllMocks())

describe('listStudentGrades', () => {
  it('parses the my-grades payload, which carries no updatedAt', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: myGradesPayload })

    const result = await listStudentGrades()

    expect(result).toHaveLength(2)
    expect(result[0].title).toBe('Trabalho de Álgebra')
    expect(result[0].submission?.grade).toBe(8.5)
    expect(result[1].submission).toBeNull()
  })

  it('rejects a payload missing a field the API does send', async () => {
    const withoutCreatedAt = { ...myGradesPayload[0] }
    delete (withoutCreatedAt as Partial<typeof withoutCreatedAt>).createdAt
    vi.mocked(api.get).mockResolvedValue({ data: [withoutCreatedAt] })

    await expect(listStudentGrades()).rejects.toThrow()
  })
})

describe('updateSubmission', () => {
  it('parses the PUT response, which comes back without createdAt', async () => {
    vi.mocked(api.put).mockResolvedValue({
      data: {
        id: 'sub-id-1',
        taskId: 'task-1',
        studentId: 'student-1',
        organizationId: 'org-1',
        textResponse: 'resposta corrigida',
        status: 'SUBMITTED',
        grade: null,
        feedback: null,
        attachments: [],
        createdAt: null,
        updatedAt: '2026-09-15T20:49:00.354999',
      },
    })

    const result = await updateSubmission({ taskId: 'task-1', submissionId: 'sub-id-1', textResponse: 'resposta corrigida' })

    expect(result.textResponse).toBe('resposta corrigida')
    expect(result.createdAt).toBeNull()
  })
})
