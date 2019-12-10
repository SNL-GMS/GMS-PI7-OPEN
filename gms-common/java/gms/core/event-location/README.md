# Event Location Control Service

## Description
This service provides a basic event location method where the user passes in
event information and control parameters and a new event hypothesis is returned
with a new set of location services.  The service uses a basic Geiger's method
for arrival-time, slowness, and receiver to source azimuth to calculate a
location (latitude, longitude, and depth) and origin time.  Location restraints
can be specified for any location parameter.  The service also returns the
residuals for observations from the calculated solution and solution uncertainty
for each requested restraint type.

## Running `event-location-control-service` Locally
```bash
cd gms-common/java/gms/core/event-location/event-location-control-service/integration
bash ./deploy-services.sh
```
`deploy-services.sh` builds both `event-location-control-service` and `osd-signaldetection-repository-service` locally and pulls the latest versions of other necessary containers from Artifactory.
It also...
 * Loads the full UEB test data set of `SignalDetection`s, `Event`s, and `ReferenceStation`s, providing all necessary data to run and test `event-location-control-service` locally.  (All services are deployed via `integration/docker-compose_local.yml`)
 * Loads fake test data contained in `.json` files in the `integration/test-data` folder.
 
 #### Rebuilding and Redeploying `event-location-control-service` Quickly
 Cloning and building the UEB test data as part of `deploy-services.sh` is time consuming.  Run `redeploy-local-containers.sh` to stop, rebuild, and redeploy `event-location-control-service` and `osd-signaldetection-repository-service`.
 
 #### Complete Rebuild/Redeploy
 
 Takes down all services, rebuilds `event-location-control-service` and `osd-signaldetection-repository-service`, redeploys all containers, and reloads all test data.
 
 ```bash
docker-compose -f docker-compose_local.yml down
bash ./deploy-services.sh
```

#### Hitting the Locator Service

HTTP request bodies are contained in `.json` files in `event-location-control-service/integration`.  Currently `locate-interactive.json` is set up to use the fake PCALC test data contained in the `integration/test-data` folder.
* `locate.json`
* `locate-override-parameters.json`
* `locate-override-definition.json`
* `locate-override-plugin.json` 

* `locate-interactive.json`
* `locate-interactive-override-parameters.json`
* `locate-interactive-override-definition.json`
* `locate-interactive-override-plugin.json` 
 
 ## Configuration
 
 Two locator plugins are available, Geiger and Apache.  All configuration files discussed below are located in 
 directory:
 ```
gms/core/event-location/event-location-control-service/src/main/resources/gms/core/eventlocation/control
 ```
 
 #### Geiger Locator Plugin Configuration
 
 To configure the system to use the Geiger locator plugin, put the following two lines in the file 
 `EventLocationControl.properties`:
 ```
pluginName=eventLocationGeigersPlugin
pluginVersion=1.0.0
```
 
 Configuration parameters are in the file `eventLocationConfigurationGeigers.properties`.  Each line
 of this field is of the form `parameterName=paramterValue`.  The parameters are:
 
| Parameter Name                   | Type   | Description                                             	      	      	      	      	      	     |
| :---                             | :---   | :---                                                                                                   |
| maximumIterationCount            | int    | maximum number of iterations in algorithm                                                              |
| convergenceThreshold             | float  | algorithm "converges" when percent by which sum of squared residuals decreases is less than this value |
| uncertaintyProbabilityPercentile | float  |                                                                                                        |
| earthModel                       | string | name of earth model (e.g., aka135, iasp91)                                                             |
| applyTravelTimeCorrections       | bool   | ellipticity and elevation corrections applied to travel time estimates if true                         |
| scalingFactorType                | enum   | CONFIDENCE, COVERAGE, or K_WEIGHTED. indicates the approach to computing confidence intervals          |
| kWeight                          | float  |                                                                                                        |
| aprioriVariance                  | float  |                                                                                                        |
| minimumNumberOfObservations      | int    | minimum number of defining observations required                                                       |
| convergenceCount                 | int    | number of times the convergence threshold must be met before algorithm terminates                      |
| levenbergMarquardtEnabled        | bool   | enable Levenberg-Marquardt method to accomodate linearity assumptions                                  |
| lambda0                          | float  | initial Levenberg-Marquardt dampening value                                                            |
| lambdaX                          | float  | Levenberg-Marquardt dampening multiplier                                                               |
| deltaNormThreshold               | float  | Levenberg-Marquardt loop terminates when "delta-m" step-size falls below this threshold                |
| singularValueWFactor             | float  | unused                                                                                                 |
| maximumWeightedPartialDerivative | float  | partial derivatives greater than this value cause the algorith to fail/terminate                       |
| constrainLatitudeParameter       | bool   | constrain the latitude estimate to seeded value if true                                                |
| constrainLongitudeParameter      | bool   | constrain the longitude estimate to seeded value if true                                               |
| constrainDepthParameter          | bool   | constrain the depth estimate to seeded value if true                                                   |
| constrainTimeParameter           | bool   | constrain the time  estimate to seeded value if true                                                   |
| deltamThreshold                  | float  | unused                                                                                                 |
| depthFixedIterationCount         | int    | unused                                                                                                 |
| dampingFactorStep                | float  | unused                                                                                                 |

 
 #### Apache Locator Plugin Configuration

To use the Apache locator plugin, the file, `eventLocationConfigurationApacheLm.properties`, 
should contain:
 ```
pluginName=eventLocationApacheLmPlugin
pluginVersion=1.0.0
```

Configuration parameters are in the file `eventLocationConfigurationApacheLm.properties`.  Each line
 of this field is of the form `parameterName=paramterValue`.  The parameters are:
 
| Parameter Name                   | Type   | Description                                                                                            |
| :---                             | :---   | :---                                                                                                   |
| maximumIterationCount            | int    | maximum number of iterations in algorithm                                                              |
| convergenceThreshold             | float  | algorithm "converges" when percent by which sum of squared residuals decreases is less than this value |
| uncertaintyProbabilityPercentile | float  | 	                                                                                                     |
| earthModel                       | string | name of earth model (e.g., aka135, iasp91)                                                             |
| applyTravelTimeCorrections       | bool   | ellipticity and elevation corrections applied to travel time estimates if true                         |
| scalingFactorType                | enum   | CONFIDENCE, COVERAGE, or K_WEIGHTED. indicates the approach to computing confidence intervals          |
| kWeight                          | float  |                                                                                                        |
| aprioriVariance                  | float  |                                                                                                        |
| minimumNumberOfObservations      | int    | minimum number of defining observations required                                                       |
| constrainLatitudeParameter       | bool   | constrain the latitude estimate to seeded value if true                                                |
| constrainLongitudeParameter      | bool   | constrain the longitude estimate to seeded value if true                                               |
| constrainDepthParameter          | bool   | constrain the depth estimate to seeded value if true                                                   |
| constrainTimeParameter           | bool   | constrain the time  estimate to seeded value if true                                                   |


