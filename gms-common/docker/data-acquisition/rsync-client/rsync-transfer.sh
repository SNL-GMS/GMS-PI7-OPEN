#!/bin/bash

check_var_set () {
  if [ -z "${1}" ]; then
    echo "Environment variable ${1} is not set, exiting"
    exit 1
  fi
}

check_var_set CI_CURRENT_PROJECT
check_var_set CI_CURRENT_ENV
# Typically set to: gmsuser
check_var_set RSYNC_USER
# Typically set to savm0217lxo.<domain name> or savm0218lxo.<domain name>
check_var_set RSYNC_REMOTE_HOST
check_var_set RSYNC_REMOTE_DIR
# For data frame parser, this is set to: /shared-volume/dataframes/
# For nifi, this is set to _____
check_var_set RSYNC_LOCAL_DIR

for host in ${RSYNC_JUMP_HOSTS}; do
  ssh-keyscan $host >> ~/.ssh/known_hosts
done

ssh-keyscan $RSYNC_REMOTE_HOST >> ~/.ssh/known_hosts

sourceFolder=${RSYNC_REMOTE_DIR}/${CI_CURRENT_ENV}-${CI_CURRENT_PROJECT}/

if [ "${RSYNC_JUMP_HOSTS}" ]; then
  IFS=' ' #delimiter
  read -ra ADDR <<< ${RSYNC_JUMP_HOSTS} # str is read into an array as tokens separated by IFS
  for i in "${ADDR[@]}"; do # access each element of array
    temp+="${RSYNC_USER}@$i,"
  done
  RSYNC_JUMP_HOSTS="${temp%?}"
fi

# Continuously rsync files over
while true
do
  echo "Starting rsync transfer; sourceFolder = ${sourceFolder}"
  # Copy invoices, then JSON files.
  if [ -z "${RSYNC_JUMP_HOSTS}" ]; then
    rsync --perms --chmod=a+rw -r -e "ssh -o StrictHostKeyChecking=no" --remove-source-files --ignore-existing --exclude="*.json" ${RSYNC_USER}@${RSYNC_REMOTE_HOST}:${sourceFolder} ${RSYNC_LOCAL_DIR}
    rsync --perms --chmod=a+rw -r -e "ssh -o StrictHostKeyChecking=no" --remove-source-files --ignore-existing --exclude="*.inv" ${RSYNC_USER}@${RSYNC_REMOTE_HOST}:${sourceFolder} ${RSYNC_LOCAL_DIR}
  else
    rsync --perms --chmod=a+rw -r -e "ssh -o StrictHostKeyChecking=no -A -J ${RSYNC_JUMP_HOSTS}" --remove-source-files --ignore-existing --exclude="*.json" ${RSYNC_USER}@${RSYNC_REMOTE_HOST}:${sourceFolder} ${RSYNC_LOCAL_DIR}
    rsync --perms --chmod=a+rw -r -e "ssh -o StrictHostKeyChecking=no -A -J ${RSYNC_JUMP_HOSTS}" --remove-source-files --ignore-existing --exclude="*.inv" ${RSYNC_USER}@${RSYNC_REMOTE_HOST}:${sourceFolder} ${RSYNC_LOCAL_DIR}
  fi

  sleep 15
done
