# [feat] favicon e imagem Open Graph para preview social

## Objetivo
Dar identidade visual ao LMS Nexus na aba do navegador e ao compartilhar links
(WhatsApp, LinkedIn, Slack etc.).

## Comportamento esperado
- O web app (`apps/web`) exibe favicon próprio, incluindo `apple-touch-icon`.
- O `index.html` traz meta tags Open Graph/Twitter Card (`og:title`,
  `og:description`, `og:image` 1200x630, `og:url` https://lmsnexus.com.br).
- Colar o link gera preview com imagem.

## Fora de escopo
- OG dinâmico por página/organização.
- PWA manifest completo.
- SEO além das meta tags de preview.
