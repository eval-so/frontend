#!/usr/bin/env bash
network=irc.tenthbit.net
channel=programming
repo=eval-so/frontend
export _JAVA_OPTIONS="-Xms256m -Xmx512m"
results=`mktemp`
sbt update | sed -r "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]//g" > $results
sbt test | sed -r "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]//g" >> $results

statuscode="$?"
url=`http -f POST https://www.refheap.com/api/paste contents="$(cat $results)" private=true | python -c 'import json,sys;print json.load(sys.stdin)["url"]+"/raw"'`
http -f POST http://rcmp.tenthbit.net/$network/$channel payload="{\"custom_ci\": 1, \"commit\": \"$(git log -1 --format=%h)\", \"branch\": \"$(git rev-parse --abbrev-ref HEAD)\", \"repository_name\": \"$repo\", \"results_url\": \"$url\", \"status\": \"$statuscode\"}"
