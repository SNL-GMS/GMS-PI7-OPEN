ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

# Copy in the test data files and loader tools.
COPY w /standard-test-data-set/w
COPY FkSpectra /standard-test-data-set/FkSpectra
COPY feature-prediction /standard-test-data-set/feature-prediction
COPY scripts /standard-test-data-set/scripts
COPY waveform-loader /standard-test-data-set/waveform-loader
COPY gms_test_data_set /standard-test-data-set/gms_test_data_set

WORKDIR /standard-test-data-set/scripts

# Environment variables used by the `upload-test-data-set.sh` script.
# With $GMS_LOAD_WAVEFORMS_WITHOUT_GRADLE defined, the
# `scripts/upload-channel-segments.sh` script will run the waveform loader tool
# embedded in the Docker image without using Gradle.  The addition to $PATH
# allows `scripts/upload-channel-segments.sh` to run `waveform-loader` easily.
ENV SD_URL=osd-signaldetection-repository-service:8080 \
    STATION_REF_URL=osd-stationreference-coi-service:8080 \
    WAVEFORMS_URL=osd-waveforms-repository-service:8080 \
    GMS_LOAD_WAVEFORMS_WITHOUT_GRADLE=1 \
    PATH=/standard-test-data-set/scripts:/standard-test-data-set/waveform-loader/bin:${PATH}

# The default command will load all of the test data into running OSD services.
CMD ["upload-test-data-set.sh", "/standard-test-data-set/gms_test_data_set", "/standard-test-data-set/FkSpectra/ChanSeg", "/standard-test-data-set/w"]

# This command will copy the data needed by the
# `interactive-analysis-api-gateway` into the (empty)
# `interactive-analysis-api-gateway-volume` mounted at `/api-gateway-volume`.
#CMD ["load-api-gateway.sh", "/standard-test-data-set", "/api-gateway-volume/Standard_Test_Data"]
