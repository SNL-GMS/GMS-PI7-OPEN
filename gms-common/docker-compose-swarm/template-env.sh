#!/bin/bash

if [[ -z "$VERSION" ]] || [[ -z "$DOCKERTREE_VERSION" ]] || [[ -z "$GMS_STACK" ]]; then
  echo \
  "ERROR: Variables 'VERSION' and 'DOCKERTREE_VERSION' must be set to the Docker Tag versions you wish to deploy
       to the Swarm Stack. i.e:

       > export VERSION=latest
       > export DOCKERTREE_VERSION=latest
       > export GMS_STACK=release
       > source release-env.sh
       "
fi

export BASE_DOMAIN=example.company.com
export CI_DOCKER_REGISTRY=localhost
export COLLECTION=/GMS
export SUBDOMAIN=gms
