#!/bin/sh

if [ -z "$GATEWAY_HOST" ]; then
    export GATEWAY_HOST="localhost"
fi

if [ -z "$GATEWAY_PORT" ]; then
    export GATEWAY_PORT=8080
fi

/bin/sed -i "s|__GATEWAY_HOST__|$GATEWAY_HOST|g" /etc/nginx/conf.d/score-web.conf
/bin/sed -i "s|__GATEWAY_PORT__|$GATEWAY_PORT|g" /etc/nginx/conf.d/score-web.conf

exec "$@"
