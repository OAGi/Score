FROM node:20-alpine
WORKDIR usr/scr/app
COPY package.json package-lock.json ./
RUN npm install
RUN npm run build
COPY . .
CMD ["node", "dist/main"]
