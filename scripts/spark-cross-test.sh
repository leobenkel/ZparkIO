#!/usr/bin/env bash
set -e

# https://stackoverflow.com/a/12916758/1450817
while read SPARK_VERSION || [ -n "$SPARK_VERSION" ]; do
  echo ">>> spark version: $SPARK_VERSION"

  COMMAND="sbt -DsparkVersion=\"$SPARK_VERSION\" \"; project root; clean ; +test\""

  echo ">>> Run '$COMMAND'"
  eval $COMMAND
done < ./sparkVersions
