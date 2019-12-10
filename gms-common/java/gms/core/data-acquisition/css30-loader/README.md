cssloader

Two docker containers are available to run the cssloader project: (1) cssloader-osdgateway-service, (2) cssloader-client

The cssloader-osdgateway-service docker container can be started by running the build_start_docker_service.sh bash script.  An instance of InfluxDB and Postgres must be running for successful execution of the service.
The cssloader-client contain can be started by running the build_start_docker_client.sh bash script.  Although, since the client is a Java application it can also be readily executed on the localhost instead of within a docker container.

