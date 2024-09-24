#!/bin/bash

state=$(docker container inspect --format="{{.State.Running}}" "selenium.chrome" 2>/dev/null)
if [[ ($? == 0) && ($state == "true") ]]; then :;
  else docker run -d -p 4321:4321 -p 5900:5900 -v /dev/shm:/dev/shm --name selenium.chrome selenium/standalone-chrome-debug
fi

java -jar -DappConfigPath=./env/local/local.yml -DbotConfigPath=./env/local/tg_bot.yml ./build/libs/tg_news_bot-0.0.1.jar
