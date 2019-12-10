# Docker CE Deployment Prerequisites

These are the hardware and software requirements necessary to run GMS.

## Hardware Requirements

* 1 machine (or VM) with at least these specs:
  * 16 CPU
  * 64GB
  * 600GB

## Software Requirements

* Centos 7.1, RHEL 7.4 or Ubuntu >= 18.04
* Docker CE >= 18.09

## Docker CE Installation

To install Docker CE, follow the Docker guide according to your linux distribution:

[Install Docker CE on **Centos**](https://docs.docker.com/install/linux/docker-ce/centos/)

[Install Docker CE on **RHEL**](https://docs.docker.com/install/linux/docker-ce/rhel/)

[Install Docker CE on **Ubuntu**](https://docs.docker.com/install/linux/docker-ce/ubuntu/)

### Post-Install

For linux installs, it is recommended that you review these [post-install instructions](https://docs.docker.com/install/linux/linux-postinstall/).
At a minimum, follow the "[Starting on Boot section](https://docs.docker.com/install/linux/linux-postinstall/#configure-docker-to-start-on-boot)".

## Node Constraints
Docker Swarm allows more than one node to be part of a cluster.  In order for GMS to work, it is necessary to "pin" certain services to run on one node in order to correctly share disk
storage and to guarantee consistent volume allocation.  These instructions will "pin" all of the necessary services to your single node.
To accomplish this, from the manager node (terminal) you will run the following script:

* `cd $GMS_INSTALL_DIR/gms-common/docker-compose-swarm`
* `./set_constraints.sh` 

The file contains:
```
#!/bin/bash -x
node_id=`docker node ls | grep Leader | awk '{print $1}'`

docker node update --label-add ${SUBDOMAIN}.cassandra=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.cd11-data-acq=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.nifi-data-acq=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.interactive-analysis-api-gateway=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.nifi-registry=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo1=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo2=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo3=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.postgresql-stationreceiver=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.wiremock=true ${node_id}
```

## Wildcard DNS

Setting up a wildcard DNS is necessary to expose the docker host for user access and
send HTTP requests to the services using an easily formatted URL configured by traefik.
The alternative to a wildcard DNS solution would be to expose the ports in the docker-compose file. HTTP access to the services
would then be granted through `<host>:<port>` and traefik would be bypassed. Since traefik is deployed with the GMS system and comes
pre-configured, instructions here are for a wilcard DNS solution. For more help on how to configure a `<host>:<port>` solution in
docker-compose, follow [this Docker guide](https://docs.docker.com/compose/compose-file/#ports).

Using the example below, log in to your DNS server and create a new zone file:

#### Example zone file for wildcard DNS
```
$TTL 3600       ; 1 hour
@       IN      SOA     taurus.example.com. dnsadmin.example.com. (
                                2       ; serial
                                1800    ; refresh (30 minutes)
                                900     ; retry (15 minutes)
                                604800  ; expire (1 week)
                                3600    ; minimum (1 hour)
                                )
@               NS      ns3.example.com.
                NS      ns4.example.com.
```

#### Section to add to enable wildcard DNS
Change the below ips appropriately:
```
; zone name
                A       192.0.2.4
; wildcard
*               A       192.0.2.4
```
