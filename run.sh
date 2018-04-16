#!/usr/bin/env bash

mvn clean package dockerfile:build
docker stop foodqueue_app_1 foodqueue_mongo_1
docker rm foodqueue_app_1 foodqueue_mongo_1
docker-compose up