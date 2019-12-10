#!/usr/bin/env bash
SLEEP_PERIOD=2
SLEEP_MAX=120
SLEEP_TOTAL=0

cd ../
gradle docker
cd integration

cd ../../../../../shared/mechanisms/object-storage-distribution/osd-signaldetection-repository-service/
gradle docker
cd ../../../../core/signal-detection/association/signal-detection-association-control-service/integration

cd ../../../../../shared/mechanisms/object-storage-distribution/osd-stationreference-coi-service/
gradle docker
cd ../../../../core/signal-detection/association/signal-detection-association-control-service/integration

# Bring up the services
printf "Bringing up services...\n"
if ! docker-compose -f docker-compose_local.yml up -d
then
    _exit 1
fi
printf "\n"

bash ./scripts/generate-data.sh $1

# Wait for coming up, then try to curl
printf "Waiting for services to pass healthchecks...\n"
while [[ $(docker ps | grep 'health: starting' | wc -l) -ne 0 ]]
do
    if [[ $(docker ps | grep 'unhealthy' | wc -l) -ne 0 ]]
    then
      printf "One or more containers is unhealthy."
    fi

    sleep ${SLEEP_PERIOD}s
    SLEEP_TOTAL=$((SLEEP_TOTAL + SLEEP_PERIOD))
    if [[ SLEEP_TOTAL -ge SLEEP_MAX ]]
    then
        printf "Containers failed to pass healthchecks in %d seconds\n" ${SLEEP_MAX}
        exit 1
    fi
    printf "%d...\n" ${SLEEP_TOTAL}
done

printf "Uploading Signal Detections to osd-signaldetection-repository-service..."
curl -X POST http://localhost:8081/coi/signal-detections -d @signal-detections.json --header "Content-Type: application/json"
printf "\nUploading Stations to osd-signaldetection-repository-service...\n"
curl -X POST http://localhost:8082/coi/reference-stations -d @station.json --header "Content-Type: application/json"

rm signal-detections.json
rm station.json

printf "\nDONE\n"
