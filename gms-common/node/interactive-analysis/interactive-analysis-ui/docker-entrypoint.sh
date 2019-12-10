#! /bin/bash
set -eu
cat /etc/nginx/nginx.template | envsubst '$GRAPHQL_PROXY_URI $WAVEFORMS_PROXY_URI $SUBSCRIPTIONS_PROXY_URI' > /etc/nginx/nginx.conf
exec nginx -g 'daemon off;'