#!/usr/bin/env bash
set -e

function run() {
  set +e
  SPARK_VERSION=$1

  echo ">>> Starting spark version: $SPARK_VERSION"

  LOG_FILE="logs/$SPARK_VERSION.log"
  EXIT_CODE_FILE="logs/$SPARK_VERSION.exitcode"
  SOCKET_LOCATION="logs/${SPARK_VERSION}-custom-socket-location"

  sbt \
    --no-server \
    --batch \
    -Dsbt.server.forcestart=true \
    -Dsbt.server.socketlocation="$SOCKET_LOCATION" \
    -DsparkVersion="$SPARK_VERSION" \
    "; project root; +clean ; +compile" &> ${LOG_FILE}

  echo $? > ${EXIT_CODE_FILE}

  if [ $? -eq 0 ]; then
    echo ">>> Completed successfully for spark version: $SPARK_VERSION"
  fi
  set -e
}

rm -r logs
mkdir -p logs

# https://stackoverflow.com/a/12916758/1450817
while read SPARK_VERSION || [ -n "$SPARK_VERSION" ]; do
  run $SPARK_VERSION &
done < ./sparkVersions

# Wait for all background jobs to complete
wait

for EXIT_CODE_FILE in logs/*.exitcode; do
  if [ "$(cat "$EXIT_CODE_FILE")" -ne 0 ]; then
    echo ">>> Failed: $EXIT_CODE_FILE with $(cat $EXIT_CODE_FILE)"
    BROKEN_SPARK_VERSION=${EXIT_CODE_FILE%.exitcode}
    cat "${BROKEN_SPARK_VERSION}.log"
  fi
done
