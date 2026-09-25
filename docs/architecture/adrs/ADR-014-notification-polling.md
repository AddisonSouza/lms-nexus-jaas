# ADR-014 — Notificações por Polling em vez de Push

**Status:** Aceito  
**Data:** Setembro 2026 (registra decisão implementada no RF de notificações)

## Contexto
Alunos e professores recebem notificações (tarefa publicada, entrega recebida, avaliação, aviso). Era preciso decidir como o front descobre notificações novas: polling HTTP, Server-Sent Events ou WebSocket.

## Decisão
Polling via TanStack Query: `useNotifications` usa `refetchInterval: 30_000`. No back-end, as notificações são criadas por observers de Domain Events do módulo `communication` e o contador de não lidas é mantido no Redis (`communication:unread-count:{userId}`), para a consulta frequente não custar no MySQL.

## Justificativa
- Notificações de LMS não exigem latência de segundos; 30 s é aceitável para o domínio
- Reaproveita o fluxo existente (Axios + JWT + refresh) sem conexão persistente para autenticar
- SSE/WebSocket exigiriam configuração extra de proxy no nginx, reconexão e fan-out — com uma única instância (ADR-012) isso não se paga
- TanStack Query já pausa o polling em aba inativa e deduplica requisições

## Consequências
- Uma requisição a cada 30 s por usuário logado, mesmo sem novidade
- Atraso de até 30 s entre o evento e a exibição
- Reavaliar SSE se surgir requisito de tempo real (ex: chat) ou se o volume de usuários tornar o polling caro
