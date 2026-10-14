# syntax=docker/dockerfile:1

# ---- Build stage: compile the Vite production bundle ----
FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies first so this layer is cached until the lockfile changes.
COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the source and produce the static bundle in /app/dist.
COPY . .

# API base URL baked into the bundle at build time. Pass with:
#   docker build --build-arg VITE_API_BASE=https://api.example.com/api .
# If unset, the bundle falls back to the relative /api path.
ARG VITE_API_BASE
ENV VITE_API_BASE=${VITE_API_BASE}
RUN npm run build

# ---- Runtime stage: serve the static bundle with nginx ----
FROM nginx:alpine

# SPA-aware nginx config (history fallback + asset caching).
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Ship only the build output, not the toolchain or source.
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173

CMD ["nginx", "-g", "daemon off;"]
