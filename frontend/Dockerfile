FROM node:20

WORKDIR /app
COPY . .

RUN npm i

ARG PORT=9533
ENV PORT=$PORT

EXPOSE $PORT

ENTRYPOINT exec npx vite --port $PORT --host 0.0.0.0