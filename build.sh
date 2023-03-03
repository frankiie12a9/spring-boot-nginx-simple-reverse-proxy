#!/bin/bash

set -e

DB_PW=""
DB_URL="jdbc:postgresql://local-psql:5432/studentgradetdd"
DB_USERNAME=""
read -p "Enter <DB_USERNAME>: " DB_USERNAME
read -p "Enter <DB_PW>: " DB_PW

IMAGE=""
TAG=""
read -p "Enter <IMAGE>: " IMAGE 
read -p "Enter <TAG>: " TAG

docker build --no-cache -t $IMAGE:$TAG  \
  --build-arg JAR_FILE=./target/tdd-0.0.1-SNAPSHOT.jar \
  --build-arg DB_SOURCE_PW=$DB_PW \
  --build-arg DB_SOURCE_URL=$DB_URL \
  --build-arg DB_SOURCE_USERNAME=$DB_USERNAME \
  . 