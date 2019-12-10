#!/bin/bash

usage (){
  echo "Usage: ./set_hosts.sh <hostname>"
  exit 1
}

if [ -z "$1" ]; then
    echo "Empty list passed in - exiting with success but not updating hosts."
    exit 0
fi

ip_addr=$( ping -c1 $1 | sed -nE 's/^PING[^(]+\(([^)]+)\).*/\1/p' )

printf "%s\t%s\n" $ip_addr $1 | tee -a /etc/hosts > /dev/null
