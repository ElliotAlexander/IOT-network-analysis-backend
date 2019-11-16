#!/bin/bash

set -Eeuo pipefail

cd "$(dirname "$0")/.."

! [ -d "./build/" ] && echo Failed to find build directory. Have you built the project? && exit 1

java -jar ./build/GDP-Group-31-Backend-Shaded.jar
