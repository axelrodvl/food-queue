#!/usr/bin/env bash

mvn clean package dockerfile:build
docker-compose up