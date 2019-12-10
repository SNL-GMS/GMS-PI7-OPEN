# Maintenance

Here you will find instructions for common maintenance tasks you may need to perform on the system. Each task below assumes that you are connected
to the docker swarm cluster via UCP on a terminal.

## Starting the System

Starting the system is the essentially the same process as initially deploying the system, as described in the [deploy section](/docs/deploy).
It's important to keep the startup order in place when bringing up the stacks (as noted below). From the location of the docker-compose files:
1. Traefik: `docker stack deploy --prune --compose-file  docker-compose-swarm-traefik.yml gms_traefik`
2. GMS: `docker stack deploy --prune --compose-file docker-compose-swarm-gms.yml ${GMS_STACK}`
3. Data Diode: `docker stack deploy --prune --compose-file docker-compose-swarm-data-acq.yml gms_data_diode`

Running the docker stack deploy commands on a stack that is already running will merely update the services. Updates can include
changes to the docker compose file or changes to the docker image.

## Stopping the System

To stop the entire system, you must bring down the stacks. As the startup order is important, so is the shutdown order (inverse of the startup order):

1. Data Diode: `docker stack rm gms_data_diode`
2. GMS: `docker stack rm ${GMS_STACK}`
3. Traefik: `docker stack rm gms_traefik`

## Wiping the System

To completely wipe the system, you must stop the system as well as remove the persistent volumes.
To remove the volumes, perform the following:

1. List the names of the volumes
   `docker volume ls`
2. Copy and paste the name of each volume that you are going to remove from each stack (gms and data diode) into the following command:  
   `docker volume rm <volume name>` (Run this for each volume)

**Note:** If you get an error indicating that a volume cannot be removed because of a container that is still holding onto the volume, run the following steps:
1. Grab the first 5 characters from the container ID in the error message
2. Verify the container exists and is in an "Exited" state
   `docker ps -a | grep <first 5 chars of container ID>
3. If the container is not in an Exited state, that means the stack has not been brought down yet. Stop now and remove the stack first.
You can determine which stacks are running by running the following command: `docker stack ls`.
4. Run `docker rm -f <container ID>`
5. Remove the volume again

## Reloading Data

If you are looking to reload the database and the api gateway, you must bring down the GMS services and delete some volumes.

1. Scale down the services to 0:  
   `docker service ls -f "name=${GMS_STACK}" -q | xargs -I % docker service scale -d %=0`
2. Run `docker volume ls -f "name=${GMS_STACK}"`
3. Delete ONLY the GMS postgres database (not data-acq postgres db), cassandra db and api-gateway volumes:  
   `docker volume rm <gms postgres volume name>`  
   `docker volume rm <api-gateway volume name>`  
   `docker volume rm <cassandra volume name>`
   **Note:** If you get an error, see the note under the "Wiping the System" section above
4. Deploy the stack: `docker stack deploy --prune --compose-file docker-compose-swarm-gms.yml ${GMS_STACK}`
5. Rerun the data loading portion of the [deploy section](/docs/deploy) for the databases and api-gateway
6. If you want to reload NiFi, follow the same process, but delete the NiFi volume instead and reload NiFi data from the deploy section

## Exec'ing into a Container

In case you need to "login" ("exec") to the container, for example to browse files within it, follow these steps:

1. Run `docker ps | grep <service name>` (e.g. `docker ps | grep osd-signaldetection`)
2. Grab the container ID
3. Run `docker exec -it <container ID> bash`
4. You are now inside the container
5. When finished, type `exit`

## Viewing Services and Logs

1. Run `docker service ls` to view all the services across every stack
2. Run `docker service ls | grep <service name>` to view an individual service
3. To view service logs for a service, run `docker service logs <service name>` (use the -f option to follow the logs)

## Viewing Docker System Stats

In the case where you need to troubleshoot system-wide problems and gather system information about the docker cluster:

1. Run `docker system info`  
   This will show information about the entire docker system like CPU and memory, including node information as well
2. Run `docker system df`  
   This will show information about the disk space that's used and available in the cluster

## Restarting Services

1. Run `docker service scale <service name>=0`
2. Once the service is completely down, to bring it back up run `docker service scale <service name>=1`

## Pruning the Docker System

It's common for objects like images, containers and volumes in the docker system to become outdated and stale after a series of
containers restarts, multiple deployments, etc. To clear the docker system of these unused objects, follow the following steps:

**Note:** As of now, UCP does not support cluster-wide prune operations, so you must log into the node which you want to prune

1. Log in (ssh) to the node of your choice
2. Run `docker system prune`
3. Type 'y' and hit enter
3. If you would like to clean unused volumes as well, run `docker system prune --volumes`

## Debugging Common GMS Issues

| Symptom | Possible Cause | Verification | Resolution |
| ------- | -------------- | ------------ | ---------- |
| 500 Internal Server Errors from OSD Services | | | |
| | Out of Storage Space | Check for near 100% disk utilization for the Node running Postgres and/or Cassandra | Wiping Databases |
| | Out of PSQL Connections | `docker exec` into the postgres container, connect to the db: `psql xmp_metadata xmp`, and run: `SELECT sum(numbackends) FROM pg_stat_database;` The limit is 100. | Clear the idle connections:  `select pg_terminate_backend(pg_stat_activity.pid) from pg_stat_activity where pg_stat_activity.datname = 'xmp_metadata' and pid <> pg_backend_pid();` |
| No Live Data in Nifi | | | |
| | Low-side Data Acquisition could be down | Check the data-diode services are running and that `nifi-data-acq.$SUBDOMAIN.$BASE_DOMAIN` is populated and running. | Double-check that the deployment procedures listed in [deploy](../deploy) were followed. Check storage usage. |
| | IP Addresses are wrong for CD1.1 | Double-check that the IP Addresses for dataman and conman in data-acq are set to the values of their pinned swarm nodes. | Update docker-compose-data-acq.yml accordingly and review the [deploy section.](../deploy) |
| | CD1.1 Data Provider not configured to forward to your service | Verify with the data provider that your conman IP has been added | Request the admin add it if not already added |
| | Kafka Stack and Data Diode Stack not co-located on the same Node | The Kafka and Data Diode Stack need to all be located on the same node to share the dataframe volume. Verify they are all running on the same Node and only have one volume | Apply labels to the services and node as necessary to limit them to the same node. |
| | Dataframes are not moving due to permissions issues or something else | `docker exec` into frame-management and check that there are frames moving from `/shared-volume/rsynced-files` to `/shared-volume/dataframes`. If you don't see any files or there are files backing up, then it's likely a service isn't running or doesn't have access to those files. | Check that permissions allow `rw` to anyone and that all services are healthy |
| Services Failing to Start | | | |
| | Out of Storage Space | Check for near 100% disk utilization for the Node running Postgres and/or Cassandra | Wiping Databases |
| | Missing Node Placement Constraints | Check the services for | Ensure that you've followed the instructions in [deploy](../deploy) for Applying Node Labels Placement Constraints |
| Flow Files Backing up in Nifi | | | |
| | Not Enough Threads Added | Check that the Number of Maximum Timer Driven Threads and Event Driven threads in Nifi (Menu -> Controller Settings -> General) are at least 100 and 5 respectively | Configure as necessary |
