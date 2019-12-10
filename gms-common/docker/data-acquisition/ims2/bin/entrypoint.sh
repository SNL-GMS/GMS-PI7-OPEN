#!/bin/bash

proxy_port=$(echo ${ENV_PROXY} | sed 's/http\:\/\///g' | cut -f2 -d":") && \
proxy_ip=$(echo ${ENV_PROXY} | sed 's/http\:\/\///g' | cut -f1 -d":" | xargs getent hosts | awk '{ print $1 }') && \
echo http ${proxy_ip} ${proxy_port} >> /usr/local/share/proxy.conf && \
echo https ${proxy_ip} ${proxy_port} >> /usr/local/share/proxy.conf

proxychains4 -f /usr/local/share/proxy.conf flask run --host=0.0.0.0
