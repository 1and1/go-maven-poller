#! /bin/bash

# Wait for a GoCD HTTP server and download the plugin info.
# Parameters:
# 1: FILE
#
# Return code:
# 0: ok
# != 0: error

FILE=$1

grep ARTIFACT_ID ${FILE} || exit 1
grep PACKAGING ${FILE} || exit 1
grep POLL_VERSION_TO ${FILE} || exit 1
grep REPO_URL ${FILE} || exit 1
