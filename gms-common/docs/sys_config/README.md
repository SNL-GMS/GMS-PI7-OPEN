# System Configuration

Please use UCP (console) or (web), as appropriate, in a local terminal to connect to the swarm cluster and to run the commands listed below.

## Set up your environment
Follow your instructions below based upon which Docker installation you use:

### Docker EE
Docker EE (with UCP and Swarm)

1. Login to the Docker UCP (web) that was installed from the [deploy_prereqs](/docs/deploy_prereqs) section
2. Navigate to "My Profile" under your username on the left-hand side of the webpage
3. Click the "New Client Bundle" button and download to a directory of your choice, this will enable you to complete the deployment steps below.
4. Unzip the bundle `unzip <ucp_bundle.zip>` from step above and run the following command to connect to the Docker UCP swarm `source env.sh`
5. [Setup Your Environment](/docs/env_setup/README.md)

### Docker CE
Docker CE (with Swarm) start here :

1. [Setup Your Environment](/docs/env_setup/README.md)

### Load the Docker Image Tarball

**NOTE: Skip this section if you are doing a Docker CE installation**
If you are using a docker image tarball to upload the images to yours swarm environment, follow these steps to load them to the docker daemon. Otherwise, skip this section.

The quickest way to upload the tarball is via the Docker UCP (web) UI. This process will upload every image to each docker swarm node.

1. Login to the Docker UCP (web) and navigate to Shared Resources > Images
2. Click on Load Image and select the tarball file
3. Click on Upload  

The upload will take a while - upwards of 20-30 minutes or more, depending on your network. Once the upload is complete, verify that the images are present in the swarm. Open a terminal
window and via your UCP (console) connection, run `docker images`. You should see standard docker images that come with EE as well as your GMS images.

### Docker CE
Create your swarm, follow the following to create a single node swarm.  **Note:** You will already be on the node you want as the manager node.

https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/

## Configure live data parameters

**CD11**

The cd11 data transfer protocol relies heavily on static IPs and Ports. Edit the 
da-connman and da-dataman services in the $GMS_INSTALL_DIR/docker-compose-swarm/docker-compose-swarm-data-acq.yml 
file to make them match the following snippets (i.e. Add the "environment" 
sections from below ):

```
da-connman:
  image: ${CI_DOCKER_REGISTRY}/gms-common/cd11-connman:latest
  environment:
    connectionManagerIpAddress: <Updated to the host machine's IP>
    connectionManagerPort: 8041
    dataProviderIpAddress: 198.47.84.103
  ports:
    - "8041:8041"
  deploy:
    labels:
      - "com.docker.ucp.access.label=${COLLECTION}"
    restart_policy:
      condition: on-failure

da-dataman:
  image: ${CI_DOCKER_REGISTRY}/gms-common/cd11-dataman:latest
  environment:
    fsOutputDirectory: /dare-receiver/shared-volume/dataframes/
    expectedDataProviderIpAddress: 198.47.84.103
    dataConsumerIpAddress: <Updated to the host machine's IP>
  volumes:
    - cd11:/dare-receiver/shared-volume:rw
  ports:
    - "8100-8110:8100-8110"
  deploy:
    placement:
      constraints:
        - node.labels.${SUBDOMAIN}.cd11-data-acq == true
    labels:
      - "com.docker.ucp.access.label=${COLLECTION}"
    restart_policy:
      condition: on-failure
```

Notes:
- Both connectionManagerIpAddress and dataConsumerIpAddress need to be updated to the IP of the host machine
- You may need to update the ports, depending on your network

**Rsync**

Remove the rsync services in the $GMS_INSTALL_DIR/docker-compose-swarm/docker-compose-swarm-kafka.yml file since they're not needed (rsync-client-cd11 and rsync-client-seedlink).

## Configure traefik

You will need to know the values of your $BASE_DOMAIN and $SUBDOMAIN that you defined in your $GMS_INSTALL_DIR/gms-common/docker-compose-swarm/release-env.sh file.  To view the values run the following:

```
echo $SUBDOMAIN
echo $BASE_DOMAIN
```

Now substitue these literal values of <subdomain> and <base domain> in the following portions of $GMS_INSTALL_DIR/docker-compose-swarm/docker-compose-swarm-traefik.yml in your favorite text editor.  
NOTE: Make sure to change any pre-configured environment names that are there already, i.e. sandbox/release, to what is show below.  
NOTE: For simplicity, use the literal name of the subdomain in the <subdomain> strings below (the env variable could be used except in the "networks" section)

Edit the traefik settings in the "deploy" section of the traefik service to match the following:

```
        - "traefik.docker.network=traefik-net-<subdomain>"
        - "traefik.port=8080"
        - "traefik.frontend.rule=Host:traefik.<subdomain>.<base domain>"
```

Edit the "networks" section under the traefik service to match:  

```
    networks:
      - traefik-net-<subdomain>
      - traefik-net-data-diode
```

Lastly, edit the "networks" section at the bottom of the file so that the network names match:  

```
  traefik-net-<subdomain>:
      name: traefik-net-<subdomain>
      .
      .
      .
  traefik-net-data-diode:
      name: traefik-net-data-diode
```

