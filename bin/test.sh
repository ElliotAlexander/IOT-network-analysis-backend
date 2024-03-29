#!/bin/bash

set -Eeuo pipefail

cd "$(dirname "$0")/.."
[ -d "./build/" ] && rm -R build/

echo Building project...
docker-compose up test
echo Removing test build...
rm -R build/
