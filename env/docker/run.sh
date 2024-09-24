#!/bin/bash

if [[ $1 == '-1' ]]; then
    docker compose -f ./env/docker/docker-compose.yml up -d
  else
    docker compose -f ./env/docker/docker-compose.yml start
fi
