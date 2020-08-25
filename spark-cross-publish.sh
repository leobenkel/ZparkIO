#!/usr/bin/env bash
set -e

PROJECT=$1

if [ -z $PROJECT ]; then
    echo "You need to define PROJECT"
    exit 1
fi

SNAPSHOT=$2

while read -r SPARK_VERSION; do
  echo ">>> spark version: $SPARK_VERSION"

  COMMAND="sbt -DsparkVersion=\"$SPARK_VERSION\""

  if [ -z "$SNAPSHOT" ]; then
    COMMAND="$COMMAND"
  else
    COMMAND="$COMMAND 'set isSnapshot := true'"
  fi

  COMMAND="$COMMAND \"; project $PROJECT; clean ; ci-release\""

  echo ">>> Run '$COMMAND'"
  eval $COMMAND
done < ./sparkVersions
