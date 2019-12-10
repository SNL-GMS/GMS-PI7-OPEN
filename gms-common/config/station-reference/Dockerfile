ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

ARG STATION_REF_DATA=station_ref_data

# Copy in the test data files and loader tools.
COPY upload-station-reference.sh /station_ref/scripts/
COPY $STATION_REF_DATA /station_ref/station_ref_data

WORKDIR /scripts

ENV STATION_REF_URL=osd-stationreference-coi-service:8080 \
    GMS_LOAD_WITHOUT_GRADLE=1 \
    PATH=/station_ref/scripts:${PATH}

# The default command will load all of the test data into running OSD services.
ENTRYPOINT ["upload-station-reference.sh", "/station_ref/station_ref_data"]
