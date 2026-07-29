import { useState } from 'react'
import { ChevronRight, Pencil, Trash2, Plus } from 'lucide-react'
import type { TopicWithContents, SubjectContent } from '../types'
import ContentCard from './ContentCard'
import { Card } from '@components/ui/card'

interface Props {
  topicsWithContents: TopicWithContents[]
  canManage: boolean
  apiBaseUrl: string
  onEditTopic: (topicId: string, currentTitle: string) => void
  onDeleteTopic: (topicId: string, title: string) => void
  onAddContent: (topicId: string) => void
  onEditContent: (content: SubjectContent) => void
  onDeleteContent: (content: SubjectContent) => void
}

function TopicList({
  topicsWithContents,
  canManage,
  apiBaseUrl,
  onEditTopic,
  onDeleteTopic,
  onAddContent,
  onEditContent,
  onDeleteContent,
}: Props) {
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>({})

  const toggle = (topicId: string) =>
    setCollapsed((prev) => ({ ...prev, [topicId]: !prev[topicId] }))

  if (topicsWithContents.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhum tópico cadastrado ainda.</p>
  }

  return (
    <div className="space-y-2">
      {topicsWithContents.map(({ topic, contents }) => {
        const isCollapsed = collapsed[topic.id] ?? false
        return (
          <Card key={topic.id} elevation="sm" className="gap-2">
            <div
              className="flex cursor-pointer items-center gap-3"
              onClick={() => toggle(topic.id)}
            >
              <ChevronRight
                className={`h-[18px] w-[18px] shrink-0 text-muted-foreground transition-transform ${isCollapsed ? '' : 'rotate-90'}`}
              />
              <span className="flex-1">
                <span className="block font-heading text-[18px] leading-tight">{topic.title}</span>
                <span className="text-xs text-muted-foreground">{contents.length} item(s)</span>
              </span>

              {canManage && (
                <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
                  <button
                    onClick={() => onAddContent(topic.id)}
                    className="rounded-full p-1.5 text-muted-foreground hover:bg-muted hover:text-foreground"
                    title="Adicionar conteúdo"
                  >
                    <Plus className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => onEditTopic(topic.id, topic.title)}
                    className="rounded-full p-1.5 text-muted-foreground hover:bg-muted hover:text-foreground"
                    title="Editar tópico"
                  >
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button
                    onClick={() => onDeleteTopic(topic.id, topic.title)}
                    className="rounded-full p-1.5 text-muted-foreground hover:bg-muted hover:text-destructive"
                    title="Excluir tópico"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              )}
            </div>

            {!isCollapsed && (
              <div className="flex flex-col gap-1 pl-[30px]">
                {contents.length === 0 ? (
                  <p className="text-xs text-muted-foreground">Nenhum conteúdo neste tópico.</p>
                ) : (
                  contents.map((c) => (
                    <ContentCard
                      key={c.id}
                      content={c}
                      canManage={canManage}
                      apiBaseUrl={apiBaseUrl}
                      onEdit={onEditContent}
                      onDelete={() => onDeleteContent(c)}
                    />
                  ))
                )}
              </div>
            )}
          </Card>
        )
      })}
    </div>
  )
}

export default TopicList
