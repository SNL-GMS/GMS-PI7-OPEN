set -x

request_command="curl 'http://${NIFI_SERVICE_NAME}:${NIFI_PORT}/nifi-api/controller/registry-clients' \
-X POST \
-H 'Content-Type: application/json' \
-H 'Accept: application/json, text/javascript, */*; q=0.01' \
--data-binary '{\"revision\":{\"version\":0},\"uri\":\"http://${NIFI_SERVICE_NAME}:${NIFI_PORT}/nifi-api/controller/registry-clients/\",\"permissions\":{\"canRead\":true,\"canWrite\":true},\"component\":{\"name\":\"${NIFI_REGISTRY_HOST}\",\"description\":\"\",\"uri\":\"http://${NIFI_REGISTRY_HOST}:${NIFI_REGISTRY_HTTP_PORT}\"}}'"

registry_create_resp=`eval "${request_command}"`

echo $registry_create_resp | jq type 2> /dev/null

if [ $? -ne 0 ]; then
    echo -e "WARNING: Unable to create registry client.\n\tNifi registry client already exists."
    exit 0
fi

registry_id=$(echo $registry_create_resp | jq -r .id)

curl -X GET http://${NIFI_SERVICE_NAME}:${NIFI_PORT}/nifi-api/controller/registry-clients/${registry_id} | jq type 2> /dev/null

if [ $? -ne 0 ]; then
    echo -e "WARNING: Unable to create registry client.\n\tIs your Nifi Instance running?"
    exit 0
fi
