# ADR-009 — React SPA em vez de Next.js

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Decidir entre React SPA (Vite) e Next.js para o front-end.

## Decisão
React SPA com Vite.

## Justificativa
- O LMS é uma aplicação 100% autenticada — SSR/SSG não agrega valor para páginas protegidas
- Vite oferece HMR instantâneo e DX superior
- Deploy simples como arquivos estáticos em qualquer CDN/nginx
- Elimina complexidade de server components, RSC e hidratação
- A API REST do Quarkus é o back-end — não o Next.js

## Reavaliação
Considerar Next.js apenas se uma área pública com SEO for adicionada ao produto.

## Consequências
- Deploy do front como arquivos estáticos
- Toda autenticação via JWT no cliente (Axios interceptors)
- Sem SSR — loading states explícitos em todas as queries
