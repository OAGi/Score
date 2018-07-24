#!/bin/sh

docker build -f Dockerfile-$1 -t oagi1docker/srt-web:$1-mysql .
