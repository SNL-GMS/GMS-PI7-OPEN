# GMS Api Gateway for Interactive Analysis UI

Gateway from the GMS Analysis UI to the rest of the system.

## Development

The `.npmrc` file in that should be used is configured to use the GMS Artifactory server on the SRN as a pass-through cache.  This is no longer stored in our project but should be placed in the root directory of a developers computer. You may need to modify or remove this file if running on a non-SRN network.

Install project dependencies:

```bash
[.../interactive-analysis-api-gateway] $ npm install
```

Test the project:

```bash
[.../interactive-analysis-api-gateway] $ npm test
```

Run the project:

```bash
[.../interactive-analysis-api-gateway] $ npm start
```

## Exploring the GraphQL API via GraphiQL

Running the Apollo GraphQL server with the above command exposes HTTP interfaces for submitting GraphQL queries, mutations and subscriptions. In addition, the server exposes a GraphiQL interface. GraphiQL is an in-browser GraphQL IDE that supports interactively building and running queries, mutations and subscriptions. In order to access the GraphiQL IDE, navigate to the following URL in your browser.

<http://localhost:3000/graphql>

## Deployment

To build the api-gateway docker container, execute the following docker build command after completing the build instructions.

```bash
[.../interactive-analysis-api-gateway] $ sudo docker build -t gms/api-gateway:<tag> .
```

where *tag* is the docker image tag you want to use identify your container image (e.g. during development use your username or *develop*). The CI pipeline will assign a version tag to official images used for deployment.

To run the api-gateway docker container, execute the following docker run command:

```bash
[.../interactive-analysis-api-gateway] $ sudo docker run -d -p3000:3000 -p4000:4000 gms/api-gateway:<tag>
```

where *\<tag\>* is the docker image tag you assigned when you built the container image.

## Demo Deployment of the Analyst User Interface & API Gateway

As an interim deployment capability pending completion of an OpenShift environment, the repository includes a docker compose file enabling deployment of the Analyst User Interface and API Gateway containers. This capability is provided primarily for near-term demos.

The docker compose file currently is not configured to pull the UI and API gateway containers from a registry; the containers must exist on the local host. The containers can be built on the local host by following the instructions in each repo's README file for building the container image. Note that the compose file expects the images to be namespaced and tagged as follows:

- `gms/analyst-ui:develop`
- `gms/api-gateway:develop`

Once the container images are available, the combined application can be started by executing the following docker compose command:

```bash
[.../interactive-analysis-api-gateway] $ docker-compose up -d
```

To stop the application and cleanup the containers:

```bash
$ docker-compose down
```

To see the application's current status:

```bash
$ docker-compose ps
```

To view the `stdout` and `stderr` output from the running application:

```bash
$ docker-compose logs

# Or, to follow the log output (like `tail -f`):
$ docker-compose logs -f
```

## Configuration

Configuration values for the API gateway application are stored in YAML files in the `config` directory and read out using the [config](https://www.npmjs.com/package/config) package.  Default values can be found in `config/default.yaml` (see the comments in that file for details).  Local overrides can be placed in `config/local.yaml` and will take precedence over the defaults.  The `config/local.yaml` file is in `.gitignore` and should not be checked into version control.

Serveral yaml files exist within the project. `config/osd_local.yaml` connects to sandbox services from a locally running API Gateway. `config/mock.yaml` turns on mock mode when running in the docker environments, while `config/osd.yaml` connects the gateway to the services in the docker environments.

The following lines can be added to your `config/local.yaml` file to decrease the rate at which new intervals get created:

```yaml
workflow:
    intervalService:
        intervalCreationFrequencyMillis: 1000000
```

## Getting test data

The gateway now utilizes the standard test data set. This is no longer contained within this repo.

To get the most up to date test data run the following python command from the `resources/` subfolder:

```python
python get_test_data.py --testDataHome <path to Test_Data_Sets>
```

After updating the test data set in developer home directory, there is a script that will update the unit test data which does live in this repo (it is subset of the STDS).
This does not need to be done per developer, just once when the STDS updates and then committed. To run the test data script run the following:

```python
python build_unit_test_data.py --stdsHomePath <path to Standard_Test_Data> --outDir test_data/additional-test-data/jest-test-data/
```

## Documentation

To generate documentation for the existing codebase, run the following from the top of the repo:

```bash
npm run docs
```

This will generate documentation for the entire project, including directories, in the `docs/ts` directory.  Documentation for the GraphQL schema will be generated in `docs/graphql`.
