# [feat] favicon e imagem Open Graph para preview social

## Summary
O LMS Nexus ganha um ícone próprio na aba do navegador e uma imagem de prévia
quando o link é compartilhado em WhatsApp, LinkedIn, Slack etc.

## Technical detail
- Hoje não há logo nem `apps/web/public/`; o header mostra só o texto "Nexus".
- Marca nova em SVG: monograma "N" em `#c67139` sobre `#f5ead8` (tokens de `index.css`).
- Assets em `apps/web/public/` (o Vite copia para a raiz do `dist`; o nginx
  serve via `try_files $uri`): `favicon.svg`, `favicon.ico` (32px),
  `apple-touch-icon.png` (180px), `og-image.png` (1200x630).
- PNG/ICO gerados uma vez a partir dos SVGs fonte e commitados — sem nova dependência no build.
- `index.html`: `<link rel="icon">` (svg + ico), `apple-touch-icon`, `meta description`,
  `og:type/site_name/title/description/url/image/image:width/height/alt`,
  `twitter:card=summary_large_image`. `og:image` com URL absoluta
  (`https://lmsnexus.com.br/og-image.png`) — WhatsApp/LinkedIn exigem.

## Scope
### In
- Favicons (svg, ico, apple-touch) e OG image estática.
- Meta tags OG/Twitter e description no `index.html`.
### Out
- OG dinâmico por página/organização; PWA manifest; SEO além do preview.
- Trocar o texto "Nexus" do header pelo logo.

## Subtasks
- [ ] Criar o SVG da marca e gerar favicon.svg, favicon.ico e apple-touch-icon.png em apps/web/public
- [ ] Criar a OG image 1200x630 (monograma + "LMS Nexus" + slogan) em apps/web/public
- [ ] Adicionar links de ícone, meta description e tags Open Graph/Twitter no index.html
- [ ] Validar build e o preview (arquivos no dist, tags no HTML servido, debugger de OG após deploy)
