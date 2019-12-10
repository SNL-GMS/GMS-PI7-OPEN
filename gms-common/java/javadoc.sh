#!/bin/bash
set -Euo pipefail
IFS=$'\n\t'

########## CONFIGURE THE BASH ERROR HANDLER ##########

function errorHandler {
  echo -e "\e[31mERROR: line $1: exit status of last command: $2\e[0m"
  exit 1
}
trap 'errorHandler ${LINENO} $?' ERR

########## Generate JavaDocs ##########

OutLog="javadoc.stdout"
ErrLog="javadoc.stderr"
if [ -f "$OutLog" ]; then
  rm "$OutLog"
fi
if [ -f "$ErrLog" ]; then
  rm "$ErrLog"
fi
./gradlew -q --console=plain alljavadoc > >(tee -a $OutLog) 2> >(tee -a $ErrLog >&2)

