#!/bin/sh

# Checks for the ${NIFI_SCRIPTS_DIR}/common.sh file and sources it
# ('.' is an alias to 'source') if it present
[ -f "${NIFI_SCRIPTS_DIR}/common.sh" ] && . "${NIFI_SCRIPTS_DIR}/common.sh"

nifi_host="${NIFI_WEB_HTTP_HOST:-${HOSTNAME}}"
nifi_port="${NIFI_WEB_HTTP_PORT:-8080}"

export no_proxy="${no_proxy},${nifi_host}"

# Set bootstrap properties
prop_replace 'java.arg.2' '-Xms2048m' ${NIFI_CONF_DIR}/bootstrap.conf
prop_replace 'java.arg.3' '-Xmx4096m' ${NIFI_CONF_DIR}/bootstrap.conf

# Establish baseline properties
prop_replace 'nifi.web.http.port'                             "${nifi_port}"
prop_replace 'nifi.web.http.host'                             "${nifi_host}"
prop_replace 'nifi.remote.input.host'                         "${nifi_host}"
prop_replace 'nifi.remote.input.socket.port'                  "${NIFI_REMOTE_INPUT_SOCKET_PORT:-10000}"
prop_replace 'nifi.remote.input.secure'                       "false"
prop_replace 'nifi.flow.configuration.file'                   "${NIFI_PERSISTENCE_DIR}/conf/flow.xml.gz"
prop_replace 'nifi.flow.configuration.archive.dir'            "${NIFI_PERSISTENCE_DIR}/conf/archive/"
prop_replace 'nifi.content.repository.directory.default'      "${NIFI_PERSISTENCE_DIR}/content_repository/"
prop_replace 'nifi.database.directory'                        "${NIFI_PERSISTENCE_DIR}/database_repository/"
prop_replace 'nifi.flowfile.repository.directory'             "${NIFI_PERSISTENCE_DIR}/flowfile_repository/"
prop_replace 'nifi.provenance.repository.directory.default'   "${NIFI_PERSISTENCE_DIR}/provenance_repository/"
prop_replace 'nifi.cluster.node.address'                      "${nifi_host}"
prop_replace 'nifi.state.management.embedded.zookeeper.start' "false"
prop_replace 'nifi.zookeeper.connect.string'                  "${ZOO_QUORUM}"
prop_replace 'nifi.cluster.is.node'                           "true"
prop_replace 'nifi.cluster.node.protocol.port'                "${NIFI_CLUSTER_PROTOCOL_PORT}"
prop_replace 'nifi.cluster.flow.election.max.wait.time'       "1 mins"

prop_add     'nifi.nar.library.directory.gms'                 "${NIFI_PERSISTENCE_DIR}/gms_processors/"
prop_add     'nifi.web.http.network.interface.eth0'           "eth0"
prop_add     'nifi.web.http.network.interface.eth1'           "eth1"

"${NIFI_HOME}/bin/nifi.sh" run &
while [ ! -f ${NIFI_LOG_DIR}/nifi-app.log ]; do sleep 2; done
tail -F "${NIFI_LOG_DIR}/nifi-app.log" &
nifi_pid="$!"

trap "printf 'Received trapped signal, beginning shutdown\n'...;" KILL TERM HUP INT EXIT;

printf "NiFi running with PID [%s]\n" ${nifi_pid}
wait ${nifi_pid}
