GMS etcd Docker Image

This **Dockerfile** create a GMS-specific **etcd** Docker image based
on the base GMS CentOS 7 Python 3.6 Pip image with the following
changes applied:

- Install and configure **etcd**
- Include a `docker-entrypoint.sh` script:
  - Update `/etc/passwd` if we are running with a numeric UID to assign that UID to the **etcd** user inside the container.
    - This may be a leftover artifact from OpenShift that is not needed anymore
  - Run /usr/bin/etc/docker-entrypoint.d/ scripts to configure **etcd** on first startup:
    - Configure **etcd** users and permissions
    - Load latest configuration values ($GMS_COMMON/config/system)[../../config/system]

Once the container is up and running, three users exist:
- User `root` is all powerful and can do anything.
- User `gmsadmin` has full readwrite permissions to all keys.
- User `gms` has read-only permissions to all the keys.

The passwords for those etcd users are set by specifying the following environment variables when the container is started for the first time:
- `GMS_ETCD_PASSWORD` sets the `gms` user's password.
- `GMS_ETCD_ADMIN_PASSWORD` sets the `gmsadmin` user's password.
- `GMS_ETCD_ROOT_PASSWORD` sets the `root` user's password.

## Building the Container:

The container requires the `gms-config` Python code to load the
configuration, as well as the configuration values.

```bash
DOCKER_REGISTRY=///
VERSION=///
$ docker-build-prep.sh
$ docker build -t ${DOCKER_REGISTRY}/etcd}:${VERSION} . 
```

## Docker Compose
```
  etcd:
    image: 'gms-etcd'
    ports:
      - 2379:2379
    environment:
      GMS_ETCD_PASSWORD: "gmsdb:gms@etcd=prevent-important-guest"
      GMS_ETCD_ADMIN_PASSWORD: "gmsdb:gmsadmin@etcd=gravity-behave-proposal"
      GMS_ETCD_ROOT_PASSWORD: "gmsdb:root@etcd=coffee-outline-taxi"
    restart: on-failure
    healthcheck:
      test: ['CMD', 'etcdctl', 'endpoint', 'health', '--user', 'gms:gmsdb:gms@etcd=prevent-important-guest']
      interval: 10s
      timeout: 10s
      retries: 10
```
