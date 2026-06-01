# ADR-006 — Shadcn/ui como Base de Componentes

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos de uma biblioteca de componentes UI para o React.

## Decisão
Shadcn/ui (sobre Radix UI) como base de componentes.

## Justificativa
- Sem vendor lock-in: componentes são copiados para o projeto (`components/ui/`)
- Baseado em Radix UI — acessível por padrão (WCAG 2.1)
- 100% customizável via Tailwind CSS
- Não engorda o bundle (tree-shaking natural)

## Consequências
- Componentes ficam em `apps/web/src/components/ui/` como código próprio
- Atualizações do Shadcn são aplicadas manualmente (positivo: controle total)
