# Grill Me

- [x] Onde fica o layout base? → **`templates/mail/`**: `mail/layout.html` Qute
  com `{#insert}`, e um template por e-mail via `@Location`, como o
  `DashboardPdfRenderer`. Nenhum Java novo em `shared/`.
- [x] Fontes do design orgânico? → **Cores Organic + fontes seguras**: CSS inline
  do tema claro; título em Georgia/serif e corpo em sans do sistema (clientes de
  e-mail ignoram fontes web).
- [x] Link de confirmação fixo em `http://localhost:5173`? → **Usar
  `lms.app.base-url`**, mesmo padrão de hoje, configurável como o do convite.
- [x] O brief está ok? → Sim.
