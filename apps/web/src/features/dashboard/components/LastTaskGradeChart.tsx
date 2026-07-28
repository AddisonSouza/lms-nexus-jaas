import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

interface Props {
  grades: number[]
}

const BUCKETS = [
  { label: '0-2', min: 0, max: 2 },
  { label: '2-4', min: 2, max: 4 },
  { label: '4-6', min: 4, max: 6 },
  { label: '6-8', min: 6, max: 8 },
  { label: '8-10', min: 8, max: 10.0001 },
]

function LastTaskGradeChart({ grades }: Props) {
  if (grades.length === 0) {
    return <p className="text-sm text-muted-foreground">Sem notas ainda.</p>
  }

  const data = BUCKETS.map(({ label, min, max }) => ({
    label,
    count: grades.filter((grade) => grade >= min && grade < max).length,
  }))

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--color-divider)" />
        <XAxis dataKey="label" fontSize={12} stroke="var(--color-text)" />
        <YAxis allowDecimals={false} fontSize={12} stroke="var(--color-text)" />
        <Tooltip />
        <Bar dataKey="count" fill="var(--color-accent)" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}

export default LastTaskGradeChart
