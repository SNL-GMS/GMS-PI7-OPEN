# Station Reference Loader

The purpose of this service is to load station reference information and
responses via the `osd-stationreference-coi-service` into Postgres.

## Background

Prior to the station reference breakout, the load of station ref info and
response files to the GMS Database was performed as part of the whole standard
test data set load.

Under pressure to provide a minimal state of GMS necessary to operate, station
reference information was identified as being the minimal set of info to
bootstrap the GMS stack. It was therefore broken out into its own service.

## Building

### Setup

Prior to building, set the STATION_REF_BASE variable for ease of use:

```
# From the root directory of the gms-common repo, run
export STATION_REF_BASE=$(pwd)
```

### Build

First, run the docker build preparation script.

```
cd $STATION_REF_BASE/config/station-reference
./docker-build-prep.sh
```

**Note:** If you ran this once, it will fail every consecutive time due to the
`station_ref_data` directory already existing. To run again, delete
`station_ref_data` first.
`rm -r station_ref_data`

Your environment is now ready to build the station_ref_loader Docker image. Run:

Note that the `CI_DOCKER_REGISTRY` environment variable should have been set in
[Setup Your Environment](/docs/env_setup/README.md).

```
# CI_DOCKER_REGISTRY needs to be set to the location of gms/centos7/openjdk9:latest
# This should have been set in the 'Setup Your Environment' section
# and could be 'localhost' or a remote docker registry.
export CI_DOCKER_REGISTRY=<registry_hostname>
cd $STATION_REF_BASE/config/station-reference
docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/station-ref-loader:${VERSION} .
```

## Usage

In order to run the loader, you simply need to mount it into the network with
the `osd-stationreference-coi-service` and postgres:

```
docker run -it --rm --network <network_name> ${CI_DOCKER_REGISTRY}/station-ref-loader:${VERSION}
```

You should see HTTP 100 and 200 responses coming back, followed by batch uploads
of response files.

## Failure recovery

If the load fails part-way through due to networking issues, service reloads,
etc. you will need to empty the databases and start from scratch.
