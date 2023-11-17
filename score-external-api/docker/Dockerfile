FROM --platform=linux/amd64 node:alpine as base

USER 0
RUN apk update
RUN apk upgrade

FROM base as deps
WORKDIR /usr/src/app
COPY --chown=node:node package*.json ./
RUN npm install
USER node

FROM base AS build
WORKDIR /usr/src/app
COPY --chown=node:node --from=deps /usr/src/app/node_modules ./node_modules
COPY --chown=node:node . .
RUN npm run build
USER node

FROM base AS runner
WORKDIR /usr/src/app
COPY --chown=node:node --from=build /usr/src/app/dist dist
COPY --chown=node:node --from=build /usr/src/app/node_modules ./node_modules
COPY --chown=node:node --from=build /usr/src/app/.env ./
USER node

EXPOSE 3000

CMD ["node", "dist/main.js"]
