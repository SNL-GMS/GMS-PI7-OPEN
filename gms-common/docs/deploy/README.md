# Deployment

## Deploy the software
1. `cd $GMS_INSTALL_DIR/gms-common/docker-compose-swarm`
1. `docker stack deploy --prune --compose-file  docker-compose-swarm-traefik.yml gms_traefik`
1. `docker stack deploy --prune --compose-file docker-compose-swarm-gms.yml ${GMS_STACK}`
1. `docker stack deploy --prune --compose-file docker-compose-swarm-data-acq.yml gms_data_diode`
4. To verify that there are three stacks running, run the following command: `docker stack ls`

## Notes for Deployment

This set of commands may need to be performed in deployment.

1. Run the following command: `docker ps | grep gms_data_diode_nifi-data-acq`
2. Capture the docker container name or ID
3. Run the following command: `docker exec -it <id> /bin/bash`
4. Run the following commands in the container:
```
mkdir /grid/frames/dataframes/
mkdir /grid/frames/frame-transfer/
chown -R 1001:root /grid/dataframes
chown -R 1001:root /grid/frame-transfer
exit
```

## Load the Station Reference Information

If you are building everything from scratch, you will likely have built this
image as something like `localhost/station-ref-loader:latest`

From Docker UCP (console):

1. `docker run -it --rm --network traefik-net-${SUBDOMAIN} ${CI_DOCKER_REGISTRY}/station-ref-loader:${VERSION}`

   **network:** represents the docker network for which the gms services reside on (to view networks run `docker network ls`)  

   **ci-docker-registry:** represents the string that is prepended to the docker image names (i.e. localhost). To view images run `docker images`  

   **version:** represents the version of the software you're deploying

## Load the data-acq database with Station References

If you are building everything from scratch, you will likely have built this
image as something like `localhost/station-ref-loader:latest`

From Docker UCP (console):

`docker run -it --rm --network traefik-net-data-diode ${CI_DOCKER_REGISTRY}/station-ref-loader:${VERSION}`  

*There should be no error messages displayed.*

## Setting Signal Detection Channel IDs

To configure the signal detection channel IDs in the database, we need to set a host variable and post an http request to it using curl.
Run this command to set the host: `host=osd-signaldetection-repository-service.${SUBDOMAIN}.${BASE_DOMAIN}`   

Next, run:  
```
curl -d '[{"id": "b9030816-6b17-4f4c-8124-c3b6d871772d","type": "BEAM","channelIds": ["4be5496e-3f24-3f6e-9f40-868b50aea26a","4c09c855-7080-3aa8-ae54-8f2be946cf92","214d6ca6-982d-3d8a-9094-c9088b43f8db","3b312784-52be-3fb9-a177-954864574a1e","5b720616-bcc4-377f-8c6c-33f6317025f2","34034f2c-8596-3d79-9f83-f4a62462eb4a","7bc9a245-3b77-3442-a1a2-7ff6990e3251","8825b92e-ad3d-35ea-9e86-46fed2a0fb19","13620c54-8a9a-3037-a57b-8b31fc0985e5","80ca3b13-1f2e-3327-af55-719624426f7d","f0cd9195-7155-377e-9ad4-f25deed6f39f","7da5ee41-35d3-3d79-aa2c-986f00fb038d","84ed58d1-f4fd-3b38-8e56-348ebbae2ce6"],"actualChangeTime": "2019-04-09T20:32:09.894493Z","systemChangeTime": "2019-04-09T20:32:09.894493Z","status": "active","comment": "PDAR filtered channel processing group"},{"id": "3d22a816-5e7c-4206-8ca4-b65784b8b4c0","type": "BEAM","channelIds": ["0e6d4442-31c7-3d23-b313-4167422c8902","b02a8d7a-feb8-383d-830b-eb26c5858f53","be2297e0-f7b0-3172-9939-e25157872d53","8d232728-e1b4-3654-87c6-ab4f0b714c26","094df832-1ba9-3ee8-9c9b-14467fb6eb15","ab78c06a-4415-3ff4-be05-09f2ff99cc4a","a250318b-4f38-33b4-b33c-2e89071f7cd0","93111d68-d560-3932-b09b-753a71a57647","dc8be70a-f5a5-32cc-83ae-aee13e359dce"],"actualChangeTime": "2019-04-09T20:32:09.894493Z","systemChangeTime": "2019-04-09T20:32:09.894493Z","status": "active","comment": "TXAR filtered channel processing group"}]' -X POST -H "Accept: application/json" -H "Content-Type: application/json" http://${host}/mechanisms/object-storage-distribution/signal-detection/channel-processing-group
```
*There should be no error messages displayed.*

**NOTE:** If you get a `Could not resolve host` error **and** you are using localhost as your base domain (i.e. no wildcard DNS), then run this curl command instead:

```
curl -d '[{"id": "b9030816-6b17-4f4c-8124-c3b6d871772d","type": "BEAM","channelIds": ["4be5496e-3f24-3f6e-9f40-868b50aea26a","4c09c855-7080-3aa8-ae54-8f2be946cf92","214d6ca6-982d-3d8a-9094-c9088b43f8db","3b312784-52be-3fb9-a177-954864574a1e","5b720616-bcc4-377f-8c6c-33f6317025f2","34034f2c-8596-3d79-9f83-f4a62462eb4a","7bc9a245-3b77-3442-a1a2-7ff6990e3251","8825b92e-ad3d-35ea-9e86-46fed2a0fb19","13620c54-8a9a-3037-a57b-8b31fc0985e5","80ca3b13-1f2e-3327-af55-719624426f7d","f0cd9195-7155-377e-9ad4-f25deed6f39f","7da5ee41-35d3-3d79-aa2c-986f00fb038d","84ed58d1-f4fd-3b38-8e56-348ebbae2ce6"],"actualChangeTime": "2019-04-09T20:32:09.894493Z","systemChangeTime": "2019-04-09T20:32:09.894493Z","status": "active","comment": "PDAR filtered channel processing group"},{"id": "3d22a816-5e7c-4206-8ca4-b65784b8b4c0","type": "BEAM","channelIds": ["0e6d4442-31c7-3d23-b313-4167422c8902","b02a8d7a-feb8-383d-830b-eb26c5858f53","be2297e0-f7b0-3172-9939-e25157872d53","8d232728-e1b4-3654-87c6-ab4f0b714c26","094df832-1ba9-3ee8-9c9b-14467fb6eb15","ab78c06a-4415-3ff4-be05-09f2ff99cc4a","a250318b-4f38-33b4-b33c-2e89071f7cd0","93111d68-d560-3932-b09b-753a71a57647","dc8be70a-f5a5-32cc-83ae-aee13e359dce"],"actualChangeTime": "2019-04-09T20:32:09.894493Z","systemChangeTime": "2019-04-09T20:32:09.894493Z","status": "active","comment": "TXAR filtered channel processing group"}]' -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Host: ${host}" http://127.0.0.1/mechanisms/object-storage-distribution/signal-detection/channel-processing-group

```

## Load GMS NiFi processors

To verify that there are 2 docker containers running, run the following command:

```
docker ps | grep ${GMS_STACK}_nifi
```

### Run Scripted copy

Run this script to copy all nar processors to all nifi images.

```
cd $GMS_INSTALL_DIR/gms-common/nifi
./copy_nars.sh
```

NiFi needs to be restarted after this is done:

```
docker service scale ${GMS_STACK}_nifi=0
docker service scale ${GMS_STACK}_nifi=1
```
```
docker service scale gms_data_diode_nifi-data-acq=0
docker service scale gms_data_diode_nifi-data-acq=1
```

## Load GMS NiFi template

**Note:** The GMS NiFi template can be copied from a currently running instance of GMS NiFi.

1. Open up a browser and navigate to the GMS NiFi UI. The URL should be in this format:  
`nifi.<subdomain>.<base domain>/nifi`
2. In the "Operate" box on the left hand side, Find the "Upload Template" button and click it.
3. On your machine, find the GMS NiFi template file and upload it. `$GMS_INSTALL_DIR/gms-common/nifi/templates/7-5-0-gms-nifi-template.xml`
4. After upload, at the top of the NiFi UI, drag the template icon onto the blank workflow area (typically the 2nd icon from the right side)
5. Select the template that you uploaded and click the "ADD" button.
6. Right click anywhere in the blank workflow space and click "Start"
7. Double click on the Station Processing group
8. Double click on the GetProcessingGroupID group
9. Double click on the LookupProcessingGroupId processor (box) 
10. Click the Properties tab
11. Click on the "Go To" arrow on the right hand side of Lookup Service
12. Click on Enable (lightning bolt) on the right hand side
13. Click the Enable button.
14. Click the Close button.
15. Click the X (top right) button.
16. Right click the LookupProcessingGroupId processor and click Start
17. In the top right hand corner of the NiFi UI, click the settings icon (the "hamburger")
18. Click "Controller Settings" and change the "Maximum timer driven thread count" to 100.
19. Click the Apply button.
20. Click the OK button. 
21. Click the X (top right) button.

## Load data-acq NiFi template

**Note:** The data-acq NiFi template can be copied from a currently running instance of data-acq NiFi.

1. Open up a browser and navigate to the GMS NiFi UI. The URL should be in this format:  
`nifi-data-acq.<subdomain>.<base domain>/nifi`
2. In the "Operate" box on the left hand side, Find the "Upload Template" button and click it.
3. On your machine, find the data-acq NiFi template file and upload it. `$GMS_INSTALL_DIR/gms-common/nifi/templates/7-5-0-data-acq-nifi-template.xml`
4. After upload, at the top of the NiFi UI, drag the template icon onto the blank workflow area (typically the 2nd icon from the right side)
5. Select the template that you uploaded and click the "ADD" button.
6. Right click anywhere in the blank workflow space and click "Start"
