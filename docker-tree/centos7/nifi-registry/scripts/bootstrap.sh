#!/bin/bash

# Update Configs
sed -i -e "s/^nifi.registry.web.http.host=.*$/nifi.registry.web.http.host=0.0.0.0/g" ${NIFI_REGISTRY_CONF_DIR}/nifi-registry.properties
sed -i "/nifi.registry.db.directory=/c\nifi.registry.db.directory=${NIFI_REGISTRY_DATABASE_REPOSITORY}" ${NIFI_REGISTRY_CONF_DIR}/nifi-registry.properties
sed -i "/run.as=/c\run.as=${NIFI_REGISTRY_USER}" ${NIFI_REGISTRY_CONF_DIR}/nifi-registry.properties

# Kick off script for self-registration with nifi
${NIFI_REGISTRY_SCRIPTS_DIR}/self_register_nifi.sh http ${NIFI_SERVICE_NAME} ${NIFI_PORT} ${NIFI_REGISTER_SLEEP} &

/usr/hdf/current/nifi-registry/bin/nifi-registry.sh run
nifi_registry_pid=${!}

tail -F ${NIFI_REGISTRY_LOG_DIR}/nifi-registry-app.log &

trap "printf 'Received trapped signal, beginning shutdown\n'...;" KILL TERM HUP INT EXIT;

printf "Nifi_Registrykeeper running with PID %s\n" ${nifi_registry_pid}.
wait ${nifi_registry_pid}
