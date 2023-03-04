#!/bin/bash

set -e

DB_PW=""
DB_URL="jdbc:h2:mem:testdb"
DB_USERNAME="sa"
read -p "Enter <DB_USERNAME>: " DB_USERNAME
read -p "Enter <DB_PW>: " DB_PW

IMAGE=""
TAG=""
PORT="1500"
read -p "> Enter <IMAGE>: " IMAGE 
read -p "> Enter <TAG>: " TAG

# remove image if it exists
if docker image inspect d$IMAGE:$TAG >/dev/null 2>&1; then
  echo "Image $IMAGE:$TAG exists. Waiting for remove it..."
  docker image rm $IMAGE:$TAG -f
fi


# NOTE: YOU MUST CHECK THE `Dockerfile` first, before choosing option 1 or 2

# image build step (option 1)
docker build --no-cache -t $IMAGE:$TAG  \
  --build-arg JAR_FILE=./target/tdd-0.0.1-SNAPSHOT.jar \
  --build-arg DB_SOURCE_PW=$DB_PW \
  --build-arg DB_SOURCE_URL=$DB_URL \
  --build-arg DB_SOURCE_USERNAME=$DB_USERNAME \
  . 

# image build step (option 2)
# docker build --no-cache -t $IMAGE:$TAG  \
#   --build-arg JAR_FILE=./target/tdd-0.0.1-SNAPSHOT.jar  .

CONTAINER="springboot-app"
NETWORK="spring-boot"
read -p "> Want to run docker container right away? (y/n)" ans
echo ""

SERVER="springboot-app"

# remove and start running mysql docker container
# docker ps -aqf name (running/not running inclusive)
# docker ps -q -f name=$SERVER (running only)
if [ "$(docker ps -aqf name=$SERVER)" ]; then
  echo "echo stop & remove old docker [$SERVER] and starting new fresh instance of [$SERVER]"
  (docker kill $SERVER || :) && (docker rm $SERVER || :)
fi

if [ "$ans" == "y" ]; then 
  # the `>/dev/null 2>&1` at the end of the command discards any output or error messages
  if docker network inspect $NETWORK >/dev/null 2>&1; then
    echo "Network $NETWORK found. Waiting for running the container..."
    docker run --name $CONTAINER -p $PORT:$PORT --network $NETWORK -d -t $IMAGE:$TAG
  else 
    echo "No network $NETWORK found. You should provide a network to join."
    exit
  fi
else 
  exit
fi