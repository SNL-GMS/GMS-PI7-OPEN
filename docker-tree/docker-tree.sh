#!/bin/bash

if [ -z $@ ]; then
  echo "Usage: ./docker-tree.sh <docker-compose command>"
  exit 1
fi

function bfs() {
  current_dir='.'
  while [[ -n $current_dir ]]; do
      for sub_dir in $current_dir/*; do
          if [[ -d $sub_dir ]]; then
              current_dir+=("$sub_dir")
              printf "%s/\n" "$sub_dir"
          else
              printf "%s\n" "$sub_dir"
          fi
      done
      current_dir=("${current_dir[@]:1}")
  done
}

composes=$( bfs | grep docker-compose.yml | sed -e 's/.//' | sed -e 's/^\///' )

for compose in $composes; do
  docker-compose -f $compose $@
done
