#
# Dockerfile for running node applications as part of the GMS project
#

# Start with the GMS node base image
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/node8:latest

# Create user and group for node.
# RUN groupadd --gid 1000 node \
#    && useradd --uid 1000 --gid node --shell /bin/bash --create-home node

# Copy in the application and fix permissions.
COPY ./resources /app/resources
COPY ./build/api-gateway.bundle.js /app/api-gateway.bundle.js
COPY ./config/ /app/config/
COPY ./node_modules/ /app/node_modules/
RUN chown -R node:root /app && \
    chmod -R g+w /app

# Set new default user, group and working directory.
USER node:root
WORKDIR /app

# Create a volume for the externally-mounted waveform binary test data files
VOLUME /opt/app-root/src/Test_Data_Sets

# Expose the GraphQL (3000) websockets (4000) ports
EXPOSE 3000 4000

# Set the default container startup command.
CMD [ "node", "./api-gateway.bundle.js" ]
