#!/bin/bash

set -e

# allow the container to be started with `-e uid=`
if [ "$uid" != "" ]; then
	# Change the ownership of /home/wiremock to $uid
	chown -R $uid:$uid /home/wiremock

	set -- gosu $uid:$uid "$@"
fi

exec "$@"
