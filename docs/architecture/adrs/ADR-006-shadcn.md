# ADR-006 — Shadcn/ui como Base de Componentes

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos de uma biblioteca de componentes UI para o React.

## Decisão
Shadcn/ui como base de componentes, no style `base-nova` (primitivas do Base UI).

## Justificativa
- Sem vendor lock-in: componentes são copiados para o projeto (`components/ui/`)
- Primitivas headless acessíveis por padrão (WCAG 2.1)
- 100% customizável via Tailwind CSS
- Não engorda o bundle (tree-shaking natural)

## Consequências
- Componentes ficam em `apps/web/src/components/ui/` como código próprio
- Atualizações do Shadcn são aplicadas manualmente (positivo: controle total)

## Revisão — Setembro 2026
O redesign do front migrou os componentes para o style `base-nova` do shadcn, que usa `@base-ui/react` no lugar do Radix (`components.json`). Do Radix restam apenas `@radix-ui/react-label` e `@radix-ui/react-slot`. A decisão de fundo — componentes copiados para o repo, sem lock-in — continua a mesma; só a primitiva por baixo mudou.
