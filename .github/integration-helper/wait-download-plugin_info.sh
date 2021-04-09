#! /bin/bash

# Wait for a GoCD HTTP server and download the plugin info.
# Parameters:
# 1: HTTP Accept header to use
# 2: GoCD Version of the server

ACCEPT=$1
GOVERSION=$2

echo "Accept: ${ACCEPT} for GoCD ${GOVERSION}"
for (( i = 0; i < 180 ; i++ )); do
  # busy loop sleep
  sleep 1
  echo -n "."

  # check docker still running
  docker ps | grep > /dev/null gocd-server || break

  # download API
  curl > plugin_info.json --silent \
    -H "Accept: ${ACCEPT}" \
    http://localhost:8153/go/api/admin/plugin_info/maven-repo \
    || continue

  # break if success
  grep ARTIFACT_ID plugin_info.json && break
done
