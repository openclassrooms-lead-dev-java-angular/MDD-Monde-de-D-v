#!/usr/bin/env bash

set -e

# Load .env
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "Error: .env file not found"
    exit 1
fi

if [ "$#" -eq 0 ]; then
    echo "Usage: ./scripts/run.sh <command> [options]"
    exit 1
fi

SPRING_ARGS="$*"

mvn \
    "-Dspring-boot.run.main-class=com.openclassrooms.mddapi.cli.MddCli" \
    "-Dspring-boot.run.arguments=$SPRING_ARGS" \
    spring-boot:run