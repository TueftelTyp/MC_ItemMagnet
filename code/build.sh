#!/bin/sh
set -eu
if [ -f ./gradlew ]; then ./gradlew build; else gradle build; fi
