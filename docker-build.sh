#!/bin/bash

echo "Building Grails application..."
./gradlew clean build

echo "Building and starting Docker containers..."
docker-compose up --build