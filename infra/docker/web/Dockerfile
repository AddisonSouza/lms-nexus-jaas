# Imagem de produção do front. O estágio final é o nginx de borda do deploy: ele
# serve a SPA e, com a config montada em runtime (infra/nginx/), termina o TLS e
# faz o proxy do /api. Não há conf embutida aqui de propósito — duplicar o
# `try_files` da SPA nos dois lugares seria uma fonte silenciosa de divergência.
# Contexto de build: apps/web

# ─── build ────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /build

# `npm ci` a partir do lockfile: instala exatamente o que foi testado e falha se
# o lock estiver fora de sincronia com o package.json.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# O Vite resolve `import.meta.env` em tempo de build, então a URL da API é
# assada na imagem — não adianta passar por env var no container. `/api` é
# relativo: o navegador bate na mesma origem e o nginx encaminha.
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ─── runtime ──────────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80 443
