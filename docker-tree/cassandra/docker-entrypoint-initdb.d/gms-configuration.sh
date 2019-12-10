#!/bin/bash

#
# This script applies GMS-specific configuration to a running Cassandra
# cluster.  It will be run by docker-entrypoint-wrapper.sh every time the
# Cassandra container is started, so care was taken to make sure that no damage
# will be done if Cassandra is already GMS-configured.  This script does not
# make any attempt to migrate an existing database if changes are made here to,
# say, the `waveforms` table schema.
#
# Overview of what happens below:
#
# 1. If the `cassandra` superuser account still has the default password set,
#    change it to ${CASSANDRA_SUPERUSER_PASSWORD}.
#
# 2. As the `cassandra` superuser, create an everyday `gms` user account (if it
#    doesn't exist), create keyspaces (if they don't exist), and grant the `gms`
#    user full control of the keyspaces.
#
# 3. As the `gms` user, create tables (if they don't exist).
#

# Define Cassandra usernames, passwords, etc.
CASSANDRA_SUPERUSER="cassandra"
CASSANDRA_SUPERUSER_DEFAULT_PASSWORD="cassandra"
CASSANDRA_SUPERUSER_PASSWORD="gmsdb:cassandra@cassandra=volcano-crop-copyright"
CASSANDRA_USER="gms"
CASSANDRA_USER_PASSWORD="gmsdb:gms@cassandra=element-act-mist"
CASSANDRA_TIMESERIES_KEYSPACE="gms_timeseries_data"
CASSANDRA_WAVEFORMS_TABLE="waveforms"
CASSANDRA_FKSPECTRA_TABLE="fk_spectra"

# Define CQL statements that we will execute to configure the database.
CQL_SET_SUPERUSER_PASSWORD="\
    ALTER ROLE ${CASSANDRA_SUPERUSER} WITH PASSWORD='${CASSANDRA_SUPERUSER_PASSWORD}';"

CQL_CREATE_ROLE="\
    CREATE ROLE IF NOT EXISTS ${CASSANDRA_USER} WITH PASSWORD = '${CASSANDRA_USER_PASSWORD}' \
    AND SUPERUSER = false \
    AND LOGIN = true;"

CQL_CREATE_KEYSPACE="\
    CREATE KEYSPACE IF NOT EXISTS ${CASSANDRA_TIMESERIES_KEYSPACE} \
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} \
    AND durable_writes = true;"

CQL_GRANT_PERMISSIONS="\
    GRANT ALL PERMISSIONS ON KEYSPACE ${CASSANDRA_TIMESERIES_KEYSPACE} TO gms;"

CQL_CREATE_TIMESERIES_TABLES="\
    CREATE TABLE IF NOT EXISTS ${CASSANDRA_TIMESERIES_KEYSPACE}.${CASSANDRA_WAVEFORMS_TABLE} ( \
        channel_id UUID, \
        date DATE, \
        start_epoch_nano BIGINT, \
        end_epoch_nano BIGINT, \
        sample_count BIGINT, \
        sample_rate DOUBLE, \
        samples FROZEN<LIST<DOUBLE>>, \
        PRIMARY KEY ((channel_id, date), start_epoch_nano) \
    ); \
    CREATE TABLE IF NOT EXISTS ${CASSANDRA_TIMESERIES_KEYSPACE}.${CASSANDRA_FKSPECTRA_TABLE} ( \
        id UUID, \
        power BLOB, \
        fstat BLOB, \
        samples_d1_size INT, \
        samples_d2_size INT, \
        PRIMARY KEY (id) \
    );"
#    ); \
#    CREATE INDEX IF NOT EXISTS idx_fk_power_spectrum_id \
#        ON ${CASSANDRA_TIMESERIES_KEYSPACE}.${CASSANDRA_FKSPECTRA_TABLE} (id);"


echo "Starting gms-configuration.sh initdb script..."

set -u

echo "Checking if we can connect to Cassandra as the superuser with the default password..."
if cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_DEFAULT_PASSWORD} -e "DESCRIBE KEYSPACES;"; then
    echo "Yes we can.  Now to change the the superuser's password..."
    set -ex
    cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_DEFAULT_PASSWORD} -e "${CQL_SET_SUPERUSER_PASSWORD}"
    set +ex
else
    echo "No we can't.  Keep going..."
fi

echo "Checking if we can connect to Cassandra as the superuser with the updated password..."
if cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_PASSWORD} -e "DESCRIBE KEYSPACES;"; then
    echo "Yes we can.  Now to create a '${CASSANDRA_USER}' everday account and a '${CASSANDRA_TIMESERIES_KEYSPACE}' keyspace..."
    set -ex
    cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_PASSWORD} -e "${CQL_CREATE_ROLE}"
    cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_PASSWORD} -e "${CQL_CREATE_KEYSPACE}"
    cqlsh -u ${CASSANDRA_SUPERUSER} -p ${CASSANDRA_SUPERUSER_PASSWORD} -e "${CQL_GRANT_PERMISSIONS}"
    set +ex
else
    echo "No we can't.  Keep going..."
fi

echo "Checking if we can connect to Cassandra as the everyday '${CASSANDRA_USER}' user..."
if cqlsh -u ${CASSANDRA_USER} -p ${CASSANDRA_USER_PASSWORD} -e "DESCRIBE KEYSPACES;"; then
    echo "Yes we can.  Now to create the timeseries tables..."
    set -ex
    cqlsh -u ${CASSANDRA_USER} -p ${CASSANDRA_USER_PASSWORD} -e "${CQL_CREATE_TIMESERIES_TABLES}"
    set +ex
else
    echo "No we can't.  Exiting with non-zero return code so that docker-entrypoint-wrapper.sh will invoke us again in a few seconds."
    exit 1
fi

echo "Success!  Cassandra is now configured for GMS."
