# [feat] favicon e imagem Open Graph para preview social — Delta

## Added
- **Marca "N" do Nexus.** Monograma geométrico (sem fonte) em `#c67139` sobre
  `#f5ead8`, fonte única em `apps/web/public/favicon.svg`.
- **Favicons.** `favicon.svg` (moderno), `favicon.ico` 16/32/48px (fallback) e
  `apple-touch-icon.png` 180px sem cantos (o iOS aplica a própria máscara).
- **Preview social.** `og-image.png` 1200x630 (monograma, "LMS Nexus" em
  Caprasimo, slogan em Figtree). `index.html` declara `meta description`,
  `og:*` (type, site_name, locale, title, description, url, image + type,
  width, height, alt) e `twitter:card=summary_large_image`, com URL absoluta
  `https://lmsnexus.com.br/og-image.png`.
- Assets gerados uma vez (Chrome headless + PIL) e versionados; o build não
  ganhou dependência.
