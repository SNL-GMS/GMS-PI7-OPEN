# Building GMS

## Setup your Environment
Make sure you've done this already before you continue with the build
* [Setup Your Environment](/docs/env_setup/README.md)

## Docker base images
In order to run any of the `docker` commands in the sections below, the docker
base images (docker tree) must be built first.  To build docker-tree, see the
"build" section of $GMS_INSTALL_DIR/docker-tree/README.md.

**Note**: You only need to build these images, you do not need to deploy them.

## How to build gms-common repository code
In order to build the gms-common environment please follow the directions for
each subdirectory below.

### Java
1. `cd $GMS_INSTALL_DIR/gms-common/java`
1. `vi gradle.properties`
1. add the following:
   ```
   # artifactory_contextUrl should be a blank string
   artifactory_contextUrl=
   # The below proxy settings should be commented out, unless you need set a proxy
   # for your network needs.
   #systemProp.http.proxyHost=
   #systemProp.http.proxyPort=
   #systemProp.http.nonProxyHost=
   #systemProp.https.proxyHost=
   #systemProp.https.proxyPort=
   #systemProp.https.nonProxyHosts=
   ```
1. `gradle build` *Note that in the gradle build command, 2 unit tests are expected to fail.  This does not preclude you to keep executing the next steps and does not prevent the setup to fail.*
1. `gradle docker`
1. `gradle publishToMavenLocal`

### nifi
1. `cd $GMS_INSTALL_DIR/gms-common/nifi`
1. `mvn install`

### Data Acquisition Frame Management High
1. `cd $GMS_INSTALL_DIR/gms-common/docker/data-acquisition/frame-management-high/`
1. `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/data-acquisition-frame-management-high:${VERSION} .`

### Data Acquisition IMS2

In order to release this code, an LGPL licensed file named `progressbar.py`
needed to be removed from the
`$GMS_INSTALL_DIR/gms-common/docker/data-acquisition/ims2/nms_client/lib`
directory.  This file needs to be replaced with the one contained within the
open source `progressbar-2.2.tar.gz` file which can be downloaded from [this
page](https://pypi.org/project/progressbar/2.2/#files).  To do this:

1. Download the `progressbar-2.2.tar.gz` file from https://pypi.org/project/progressbar/2.2/#files.
1. Untar the downloaded file with `tar -xzf progressbar-2.2.tar.gz`.
1. Move the unpacked `progressbar.py` file into the target directory by running `mv progressbar-2.2/progressbar.py $GMS_INSTALL_DIR/gms-common/docker/data-acquisition/ims2/nms_client/lib`.

Now build the `ims2_servive` Docker image:

1. `cd $GMS_INSTALL_DIR/gms-common/docker/data-acquisition/ims2`
1. `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/gms/ims2_service:${VERSION} .`

### etcd
1. `cd $GMS_INSTALL_DIR/gms-common/docker/etcd`
1. `./docker-build-prep.sh`
1. `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/etcd:${VERSION} .`

### Python
1. `cd $GMS_INSTALL_DIR/gms-common/python/master-coi-data-client`
1. `python3 setup.py install`

**Note that sudo may be required for this. This only needs to be done once. Subsequent runs can omit this step.**

### Typescript

### Prerequisites
1. `cd $GMS_INSTALL_DIR/xdc-golden-layout`
1. `npm build`
1. `sudo -E npm link`

### Building the interactive-analysis-api-gateway
1. `cd $GMS_INSTALL_DIR/gms-common/node/interactive-analysis/interactive-analysis-api-gateway`
1. `npm install`
1. `npm run build`
1. `npm run build-bundle`
1. `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/gms-common/interactive-analysis-api-gateway:${VERSION} .`

### Building interactive-analysis-ui
1. `cd $GMS_INSTALL_DIR/gms-common/node/interactive-analysis/interactive-analysis-ui/packages/analyst-ui-core`
1. `npm link @gms/golden-layout`
1. `cd $GMS_INSTALL_DIR/gms-common/node/interactive-analysis/interactive-analysis-ui`
1. `npm install`
1. `npm run bootstrap`
1. `npm run build`
1. `docker build --build-arg DOCKER_REGISTRY=${CI_DOCKER_REGISTRY} -t ${CI_DOCKER_REGISTRY}/gms-common/interactive-analysis-ui:${VERSION} .`

### Station Reference Loader

In order to bootstrap the system with the minimum set of data to receive data,
the station reference loader needs to be created.

Refer to the station reference loader [README.md](/config/station-reference/README.md#Build)
for instructions on how to build the loader. **You only need to do the "Setup" and "Build"
sections.**
