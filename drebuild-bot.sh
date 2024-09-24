#!/bin/bash

./dstop-bot.sh
./env/docker/rm-bot.sh
./env/docker/build.sh tg_news_bot_app && ./drun-bot.sh
