FROM node:20

WORKDIR /app
COPY . .

RUN npm i

ARG PORT=9533
ENV PORT=$PORT

EXPOSE $PORT

RUN npm run build

ENTRYPOINT exec npm run server