import type { SubjectAverageGrade } from '../types'

interface Props {
  subjects: SubjectAverageGrade[]
}

function SubjectAverageGradesList({ subjects }: Props) {
  if (subjects.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma média disponível ainda.</p>
  }

  return (
    <ul className="space-y-1">
      {subjects.map((subject) => (
        <li key={subject.subjectId} className="flex items-center justify-between border-b pb-1 text-sm">
          <span>{subject.subjectName}</span>
          <span className="text-muted-foreground">{subject.averageGrade}</span>
        </li>
      ))}
    </ul>
  )
}

export default SubjectAverageGradesList
