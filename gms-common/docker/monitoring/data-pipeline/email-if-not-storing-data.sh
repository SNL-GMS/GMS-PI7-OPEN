#!/bin/bash

################################################################################################################################
#This script queries postgres for channel segment storage and sends out an email if it detects data hasn't been stored recently.
################################################################################################################################



#this is the name of the persted file that if present indicates that an email is already sent or not
EMAIL_SENT_FILE=/opt/persistence/email-sent.txt

#How long to wait in seconds until notifying
#30 Minutes
THRESHOLD=1800

#Convert seconds to Hours, Minutes, Seconds
convertsecs() {
 ((h=${1}/3600))
 ((m=(${1}%3600)/60))
 ((s=${1}%60))
 printf "%02d Hours %02d Minutes and %02d Seconds\n" $h $m $s
}

while :
do
  #Query for when the latest channel segment was received
  LASTDATA=$(PGPASSWORD=${POSTGRES_PASS} psql -U ${POSTGRES_USER} -p ${POSTGRES_PORT} -h ${POSTGRES_URL} ${POSTGRES_DB} -c "SELECT creation_time FROM channel_segment ORDER BY creation_time DESC LIMIT 1;" | sed -n 3p)

  # We need to check if this resolves to something, otherwise LDUNIXTIME will result to 12am of the current day
  if [ -z "$LASTDATA" ]; then
    echo "Couldn't determine when last data was received. Check connection to psql."
  else
    #LASDATA resolved and we have a time to calculate against
  
    #Calculate time difference
    LDUNIXTIME=$(date -u +"%s" -d "$LASTDATA")
    NOW=$(date -u +"%s")
    SECS=$(($NOW - $LDUNIXTIME)) 
 
    #If difference is longer than threshold, notify
    if [ $SECS -ge $THRESHOLD ]; then
      echo "SECS = $SECS, TH = $THRESHOLD" 
      if [ ! -f $EMAIL_SENT_FILE ]; then
        mail -s "Data Stream Has Stopped" ${TO_EMAIL} <<< "This is an automated message from the docker-compose sandbox environment. $(date -u) : Data has not been stored for $(convertsecs $SECS)"
        touch $EMAIL_SENT_FILE
      else
        echo "email already sent"
      fi
    #Else we have recently received data, reset the email check
    else
      echo "Data connection up. Last storage: $(convertsecs $SECS)"
      rm -f $EMAIL_SENT_FILE
    fi
  fi
  sleep 60
done
