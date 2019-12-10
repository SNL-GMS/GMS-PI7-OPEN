# Checkout Procedures

Currently, verifying that GMS is "working" requires some amount of manual
verification.

## Checkout Prerequisites

For a full-system checkout, you'll need a web browser and a terminal with:
* `curl` installed
* ucp client bundle for the swarm cluster

#### Terminal Configuration

For instructions on getting your UCP client bundle working see [UCP](/docs/deploy_prereqs/README.md#UCP)

```
cd <ucp_directory>
source env.sh
```

## Database Checkout

The most basic check for configuration is to query PostgreSQL directly.

#### PostgreSQL

##### Live Data

Check that live data is being processed and stored in the database:

```
psql_id=$( docker ps | grep ${GMS_STACK}_postgres | cut -d' ' -f1 )
docker exec -it $psql_id /bin/bash
PGPASSWORD="gmsdb:xmp@postgres=bird-hero-calendar" psql xmp_metadata xmp

# Run this command repeatedly and look for updating timestamps. These are new data coming into the system.
select software_component_name, payload_data_start_time, payload_data_end_time, station_id, reception_time from raw_station_data_frame ORDER BY reception_time DESC limit 10;
```

##### Channel Processing Groups

Check that the channel processing groups were added correctly

```
psql_id=$( docker ps | grep ${GMS_STACK}_postgres | cut -d' ' -f1 )
docker exec -it $psql_id /bin/bash
PGPASSWORD="gmsdb:xmp@postgres=bird-hero-calendar" psql xmp_metadata xmp

# Run this command to verify that two entries are present:
select * from channel_processing_group;

# And this command to lookup all the actual channel ids:
select * from channel_processing_group_channel_ids;
```

##### Postgresql Troubleshooting

A quick # of connections check

```
select * from pg_stat_activity where datname = 'xmp_metadata';
```

Postgres query to kill idle connections (assumes you have already docker exec'd into the container, see previous step)

```
select pg_terminate_backend(pg_stat_activity.pid) from pg_stat_activity where pg_stat_activity.datname = 'xmp_metadata' and pid <> pg_backend_pid();
```

#### Cassandra

##### Data

Check that data is loaded into Cassandra

```
docker ps | grep cassandra
docker exec -it <cass_container_id> /bin/bash
cqlsh localhost -u gms -p "gmsdb:gms@cassandra=element-act-mist"
use gms_timeseries_data;

# This should create a ton of output
select * from gms_timeseries_data.waveforms LIMIT 1;
```

## Test URLs

`<subdomain>` and `<basedomain>` are often the testbed and corporate domain.
I.e. `testbed.company.org`

#### Front-End GUI Component Testing

| **Name**                    | **Test URL**                                                                      |
|-----------------------------|:----------------------------------------------------------------------------------|
| **interactive-analysis-ui** |	`http://interactive-analysis-ui.<subdomain>.<basedomain>`                         |
| **nifi**                    | `http://nifi.<subdomain>.<basedomain>/nifi-api/system-diagnostics`                |
|                             | `http://nifi.<subdomain>.<basedomain>/nifi`                                       |
| **nifi-registry**           | `http://nifi-registry.<subdomain>.<basedomain>/nifi-registry`                     |
| **nifi data acquisition**   | `http://nifi-data-acq.<subdomain>.<basedomain>/nifi-api/system-diagnostics`                |
|                             | `http://nifi-data-acq.<subdomain>.<basedomain>/nifi`                                       |

#### For Service (everything else) testing : Step-by-step guide
Below table contains all the currently defined services deployed to the GMS Docker-swarm testbeds with the routes to test the services.

From your browser, copy the URL below for your specific service.

Curl commands and web endpoints should respond with either 20x OK or a non-404
or 50x response. 404 or 50x indicates the service is either down or error-ing
out.

| **Name** | **Test URL** |
|----------|:-------------|
| **beam-control-service** |  `http://beam-control-service.<subdomain>.<basedomain>/signal-enhancement/beam/alive` |
| | (post 8.1.0) `http://beam-control-service.<subdomain>.<basedomain>/alive` |
| **filter-control-service** | `http://filter-control-service.<subdomain>.<basedomain>/signal-enhancement/waveform-filtering/alive` |
| **fk-control-service** | `http://fk-control-service.<subdomain>.<basedomain>/signal-enhancement/fk/alive` |
| **fp-service** | `curl -X POST http://fp-service.<subdomain>.<basedomain>/is-alive` |
| **interactive-analysis-api-gateway** | `http://graphql.interactive-analysis-api-gateway.<subdomain>.<basedomain>` |
| | `http://subscriptions.interactive-analysis-api-gateway.<subdomain>.<basedomain>` |
| **javadocserver** | `http://javadocserver.<subdomain>.<basedomain>` |
| **openapi-server** | `http://openapi-server.<subdomain>.<basedomain>` |
| **osd-signaldetection-repository-service** | `http://osd-signaldetection-repository-service.<subdomain>.<basedomain>/mechanisms/object-storage-distribution/signal-detection/alive` |
| **osd-stationreference-coi-service** | `http://osd-stationreference-coi-service.<subdomain>.<basedomain>/mechanisms/object-storage-distribution/station-reference/alive` |
| **osd-waveforms-repository-service** | `http://osd-waveforms-repository-service.<subdomain>.<basedomain>/mechanisms/object-storage-distribution/waveforms/alive` |
| **signal-detection-association-control-service** | `curl -X POST http://signal-detection-association-control-service.<subdomain>.<basedomain>/is-alive` |
| **signal-detector-control-service** | `http://signal-detector-control-service.<subdomain>.<basedomain>/signal-detection/signal-detector-control/alive` |
| **waveform-qc-control-service** | `http://waveform-qc-control-service.<subdomain>.<basedomain>/alive` |
| **osd-transferredfile-repository-service** | `curl -X POST http://osd-transferredfile-repository-service.<subdomain>.<basedomain>/is-alive` |
| **event-location-control-service** | `curl -X POST http://event-location-control-service.<subdomain>.<basedomain>/is-alive` |
| **transferred-file-cleanup-utility** | No current HTTP test endpoint for this service. The Links above won't provide anything. This service will attempt to delete old data from the database every 30 minutes. The only way to "test" this service is running correctly is no exceptions should be produced in the logs, normal messages will be something like: Calling the repo to remove old files transferred file cleanup utility done; will now sleep PT30M |

### UI Verification

#### Live Data

1. Open up Google Chrome and navigate to the `http://interactive-analysis-ui.<subdomain>.<basedomain>`
2. In the top right hand side, in the "Workflow" box, click the ">>" button, and click on the Intervals > "Last 24 Hours"
3. Click the "AL1 - events" box lined up in the appropriate time slot
4. Scroll down to the station that you expect to see data in
5. Double click on that station's row to view the waveforms

#### Test Data
1. Open up Google Chrome and navigate to the `http://interactive-analysis-ui.<subdomain>.<basedomain>`
2. Click the "AL1 - events" box lined up in one of the two last two time slots
3. Scroll down to the station that you expect to see data in
4. Double click on that station's row to view the waveforms

#### Nifi Data Processing
1. Open up Google Chrome and navigate to the `http://nifi.<subdomain>.<basedomain>/nifi`
2. Visually verify that you see flowfiles moving through the processors
   * This is verified by checking the "In" and "Out" across the processors, they
     should show that data is moving
   * Also check that there aren't any errors (little red boxes in the top right corner)
3. The main top-level groups (Data Acquisition, Data QC, and Station Processing)
   should each show data flowing.

#### Nifi Data Acquisition Data Processing
1. Open up Google Chrome and navigate to the `http://nifi-data-acquisition.<subdomain>.<basedomain>/nifi`
2. Visually verify that you see flowfiles moving through the processors
   * This is verified by checking the "In" and "Out" across the processors, they
     should show that data is moving
   * Also check that there aren't any errors (little red boxes in the top right corner)
3. There is only one main top-level, no drill-down
   should show data flowing.

### Python - Master Coi Verification

#### Master COI
From the master-coi-data-client folder:
* `cd $GMS_INSTALL_DIR/gms-common/python/master-coi-data-client`

To start the client, simply run the follow commands below. **Note** *that this will display a helpful menu of arguments.*
* `export PYTHONIOENCODING=utf-8`
* `python3 coidataclient.py`

Or the docker container can be built and you can run the client from there 
to build the docker image for coi_data_client:
* `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t localhost/coi_data_client .` 
to run the docker image:
* `docker exec -it localhost/coi_data_client /bin/bash`
this will get you inside of the container and you can run the above two commands (for running coidataclient.py)

