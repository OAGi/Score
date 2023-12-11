FROM --platform=linux/amd64 node:alpine as base

USER 0
WORKDIR /usr/app
RUN apk update
RUN apk upgrade
RUN npm install -g npm@latest

FROM base as deps
WORKDIR /usr/app
COPY --chown=node:node package*.json ./
RUN npm install
USER node

FROM base AS build
WORKDIR /usr/app
COPY --chown=node:node --from=deps /usr/app/node_modules ./node_modules
COPY --chown=node:node package.json ./
COPY --chown=node:node tsconfig.json ./
COPY --chown=node:node tsconfig.build.json ./
COPY --chown=node:node .env ./
COPY --chown=node:node ./src ./src
RUN npm run build
USER node

FROM base AS runner
WORKDIR /usr/app
COPY --chown=node:node --from=build /usr/app/dist ./dist
COPY --chown=node:node --from=build /usr/app/.env ./
COPY --chown=node:node --from=build /usr/app/node_modules ./node_modules
RUN mkdir cache
RUN chown node cache
USER node
EXPOSE 3000

CMD ["node", "dist/main.js"]
