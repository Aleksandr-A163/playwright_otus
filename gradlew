#!/bin/sh

DIRNAME=$(cd "$(dirname "$0")" && pwd)
exec java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
