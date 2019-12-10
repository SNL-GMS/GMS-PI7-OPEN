# Docker Tree

The purpose of the repository is to provide a structured Dockerfile directory
hierarchy that reflects the inheritance of the constituent Docker images.

## Pattern

At every level with children, there is a docker-compose.yml that describes each
image with its:
* Build ARGS
* Parent Image
* Context (directory with Dockerfile)

The Parent Image is the image built in the parent directory or an external image
if at the base.

For every child directory, there is a Dockerfile and, if necessary, a `src/`
directory to hold src files. Additionally, there is a standard `.dockerignore`
that should be added to each of these directories to prevent large context
initializations for the Docker Daemon.

## Environment

### Offsite build
If building locally (offsite) without a remote registry, first run:
```
prep-offsite-build.sh
```

The docker-compose.ymls contain environment variables that are expected to be in
your local environment. The bare minimum for the images to be built should be
exported in the `env.sh`

To source the `env.sh`:
```
source env.sh
```

## Building Automatically

A helper script is provided `docker-tree.sh` which passes the given arguments
to each compose file in breadth-first-search order. This ensures that any builds
occur in the correct ordering.

```
./docker-tree.sh build --parallel
```

## Building Manually

As reference, these images can be built layer by layer using docker-compose directly.

This process is automated in the correct order using the docker-tree script.

To build all the child images at a single layer in parallel:
```
docker-compose build --parallel
```

To build a single image, i.e. node10:
```
docker-compose build node10
```

## Deployment

The images are most easily deployed using the docker-tree.sh script.
```
./docker-tree.sh push
```
