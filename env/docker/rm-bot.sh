#!/bin/bash

yes | docker compose -f ./env/docker/docker-compose.yml rm tg_news_bot_app
