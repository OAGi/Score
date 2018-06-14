#!/bin/sh

docker build -f Dockerfile-$1 -t oagi1docker/srt-repo:$1-mysql .
