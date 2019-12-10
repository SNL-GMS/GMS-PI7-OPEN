#!/bin/bash

echo Running as:
/usr/bin/id

# Execute *.sh and *.cql scripts found in the /docker-entrypoint-initdb.d
# directory when the first Cassandra instance in a cluster starts up (e.g.,
# when $CASSANDRA_SEEDS is empty).  These scripts will be executed every time
# Cassandra starts up, so they should be written to be tolerant of that (e.g.,
# use "CREATE TABLE IF NOT EXISTS ..." instead of just "CREATE TABLE ...").
if [ "$1" = 'cassandra' ] && [[ -z "$CASSANDRA_SEEDS" ]]; then
    for f in /docker-entrypoint-initdb.d/*; do
        case "$f" in
            *.sh)     echo "$0: running $f" && until sh "$f";       do >&2 echo "Cassandra is unavailable - sleeping"; sleep 5; done & ;;
            *.cql)    echo "$0: running $f" && until cqlsh -f "$f"; do >&2 echo "Cassandra is unavailable - sleeping"; sleep 5; done & ;;
            *)        echo "$0: ignoring $f" ;;
        esac
        echo
    done
fi

# Then exec over to the normal entrypoint script from the official Docker Hub
# cassandra image.
exec /docker-entrypoint.sh "$@"
