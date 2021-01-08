#! /bin/bash
set -e
curl -X DELETE -u${JFROG_USER}:${JFROG_APIKEY} https://api.bintray.com/packages/sfuhrm/go-maven-poller/plugin/versions/$1
