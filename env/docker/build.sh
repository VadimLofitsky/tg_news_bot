#!/bin/bash

./build.sh && \
  cp ./build/libs/tg_news_bot-0.0.1.jar ./env/docker/app/ && \
  cp ./src/main/resources/tg_bot.yml ./env/docker/app/ && \
  cd ./env/docker && \
  docker compose build $1 && \
  cd ../..
