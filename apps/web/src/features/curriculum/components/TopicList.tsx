import { useState } from 'react'
import { ChevronDown, ChevronRight, Pencil, Trash2, Plus } from 'lucide-react'
import type { TopicWithContents, SubjectContent } from '../types'
import ContentCard from './ContentCard'

interface Props {
  topicsWithContents: TopicWithContents[]
  canManage: boolean
  apiBaseUrl: string
  onEditTopic: (topicId: string, currentTitle: string) => void
  onDeleteTopic: (topicId: string) => void
  onAddContent: (topicId: string) => void
  onEditContent: (content: SubjectContent) => void
  onDeleteContent: (contentId: string) => void
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
    <div className="space-y-3">
      {topicsWithContents.map(({ topic, contents }) => {
        const isCollapsed = collapsed[topic.id] ?? false
        return (
          <div key={topic.id} className="rounded-lg border">
            <div
              className="flex cursor-pointer items-center gap-2 px-4 py-3 hover:bg-muted/50"
              onClick={() => toggle(topic.id)}
            >
              <span className="text-muted-foreground">
                {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
              </span>
              <span className="flex-1 text-sm font-medium">{topic.title}</span>
              <span className="text-xs text-muted-foreground">{contents.length} item(s)</span>

              {canManage && (
                <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
                  <button
                    onClick={() => onAddContent(topic.id)}
                    className="rounded p-1 text-muted-foreground hover:text-foreground"
                    title="Adicionar conteúdo"
                  >
                    <Plus className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => onEditTopic(topic.id, topic.title)}
                    className="rounded p-1 text-muted-foreground hover:text-foreground"
                    title="Editar tópico"
                  >
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button
                    onClick={() => onDeleteTopic(topic.id)}
                    className="rounded p-1 text-muted-foreground hover:text-destructive"
                    title="Excluir tópico"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              )}
            </div>

            {!isCollapsed && (
              <div className="border-t px-4 py-3 space-y-2">
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
                      onDelete={onDeleteContent}
                    />
                  ))
                )}
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}

export default TopicList
