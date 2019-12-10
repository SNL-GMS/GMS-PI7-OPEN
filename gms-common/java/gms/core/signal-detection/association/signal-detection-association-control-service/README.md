# Signal Detection Association Control Service
## Description
This service takes a set of Signal Detection Hypothesis Descriptors and using a gridded earth-model
determines which detections can be associated to a possible event at grid points for the earth-model.

## URLs
* OpenShift Sandbox URL: https://signal-detection-association-control-service-sandbox.gms-apps.XXXXX.XXX
* OpenShift Release URL: https://signal-detection-association-control-service-release.gms-apps.XXXXX.XXX

## Endpoints
* `/event/association/associate-to-location`
  * Example request body [here](https://gitlab.XXXXX.XXX/gms/gms-common/blob/develop/java/gms/core/signal-detection/signal-detection-association-control-service/integration/test-associate-claim-check.json).
* `/event/association/associate-to-location/interactive`
  * Example request body [here](https://gitlab.XXXXX.XXX/gms/gms-common/blob/develop/java/gms/core/signal-detection/signal-detection-association-control-service/integration/test-associate-streaming.json).
* `/event/association/associate-to-event/interactive`
  * Example request body [here](https://gitlab.XXXXX.XXX/gms/gms-common/blob/develop/java/gms/core/signal-detection/signal-detection-association-control-service/integration/test-associate-to-event.json).
  
* More detailed documentation is available in the Architecture guidance (on the wiki at /display/gmswiki/Signal+Detection+Association+Domain) under in the "**Service Interfaces**" section.
  
##Running this Service Locally
1. cd into `gms-common/java/gms/core/signal-detection/signal-detection-association-control-service/integration/`
2. run `bash deploy-services.sh`
   * This script...
     1. Builds and deploys the `signal-detection-association-control-service` container
     2. Pulls and deploys latest `osd-signaldetection-repository-service` image from Artifactory
     3. Pulls and deploys latest `postgres` image from Artifactory
     4. Clones the `standard_test_data_set` repository and builds the necessary test data (`Event` and `SignalDetection` components)
     5. Loads the `Event` and `SignalDetection` test data into the running `osd-signaldetection-repository-service`
     
 `signal-detection-association-control-service` should now be running on `localhost:8080`, and `osd-signaldetection-repository-service` should be populated with all necessary test data.

## Example Queries Used Internally by this Service

The ClaimCheck association operations require querying for `SignalDetectionHypothesis` objects to associate to a new or existing `EventHypothesis`.

#### Query for single `SignalDetectionHypothesis` from the signal detection repository service

```
curl -X POST http://osd-signaldetection-repository-service-sandbox.gms-apps.XXXXX.XXX/coi/signal-detections/hypotheses/query/ids --data '{"ids":["b87787d0-68ce-38df-95f9-7dcb0b1f1fe4"]}'
```
**Response**
```
[
  {
    "id": "b87787d0-68ce-38df-95f9-7dcb0b1f1fe4",
    "parentSignalDetectionId": "b87787d0-68ce-38df-95f9-7dcb0b1f1fe4",
    "featureMeasurements": [
      {
        "id": "78585520-cf4a-46df-aee0-1573f8c942ba",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": 1.274391483749E12,
          "standardDeviation": 0.552,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "ARRIVAL_TIME"
      },
      {
        "id": "667da06a-0041-4f7e-899d-8e35e3dc8b42",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": 28.52,
          "standardDeviation": 1.26,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "SLOWNESS"
      },
      {
        "id": "2248e0b9-8ef3-4ee4-9119-602a33ac93aa",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": -1.0,
          "standardDeviation": 0.0,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "EMERGENCE_ANGLE"
      },
      {
        "id": "365b824e-8846-4eac-a87b-564fe61c74a9",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": -1.0,
          "standardDeviation": 0.0,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "RECTILINEARITY"
      },
      {
        "id": "02f97205-9c94-4f46-bedc-b7659b59a9ab",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": -1.0,
          "standardDeviation": 0.0,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "SNR"
      },
      {
        "id": "d7c8eaf8-1a7f-4aac-92ad-d71d7463f419",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": 317.18,
          "standardDeviation": 2.54,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "AZIMUTH"
      },
      {
        "id": "842a644c-81bd-494d-bed9-26a24bfcd1fa",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": -1.0,
          "standardDeviation": 0.0,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "AMPLITUDE"
      },
      {
        "id": "f7dc768a-447d-4727-94c8-ca199b5d3923",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": "Lg",
          "confidence": 1.0
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": false,
        "enumerated": true,
        "featureMeasurementType": "PHASE"
      },
      {
        "id": "0d89b4fd-69b6-4f94-a423-b29d4b682e12",
        "channelSegmentId": "deddff59-2649-32e3-8ffc-3ee116f37afd",
        "featureMeasurement": {
          "value": -1.0,
          "standardDeviation": 0.0,
          "startTime": "2010-05-20T21:38:03.749Z",
          "endTime": "2010-05-20T21:38:03.749Z"
        },
        "creationInfoId": "00000000-0000-0000-0000-000000000000",
        "numerical": true,
        "enumerated": false,
        "featureMeasurementType": "PERIOD"
      }
    ],
    "creationInfoId": "00000000-0000-0000-0000-000000000000",
    "rejected": false
  }
]
```

#### Store event in signal detection repository service

```
curl http://osd-signaldetection-repository-service-sandbox.gms-apps.XXXXX.XXX/coi/events --data '[
  {
      "id": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
      "rejectedSignalDetectionAssociations": [],
      "monitoringOrganization": "CTBTO",
      "hypotheses": [
        {
          "id": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
          "eventId": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
          "parentEventHypotheses": [],
          "rejected": false,
          "locationSolutions": [
            {
              "id": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
              "location": {
                "latitudeDegrees": 45.27125,
                "longitudeDegrees": 130.55595,
                "depthKm": 0.0,
                "time": "2010-05-20T21:37:15.048Z"
              },
              "locationRestraints": {
                "depthRestraintType": "FIXED_AT_SURFACE",
                "depthRestraintKm": null,
                "positionRestraintType": "UNRESTRAINED",
                "latitudeRestraintDegrees": null,
                "longitudeRestraintDegrees": null,
                "timeRestraintType": "UNRESTRAINED",
                "timeRestraint": null
              },
              "locationUncertainty": {
                "xx": 316.5223,
                "xy": -124.0671,
                "xz": -1.0,
                "xt": 69.3975,
                "yy": 324.0427,
                "yz": -1.0,
                "yt": -71.5326,
                "zz": -1.0,
                "zt": -1.0,
                "tt": 24.0261,
                "stDevOneObservation": 0.0,
                "ellipses": [
                  {
                    "scalingFactorType": "CONFIDENCE",
                    "kWeight": 0.0,
                    "confidenceLevel": 0.9,
                    "majorAxisLength": 45.2134,
                    "majorAxisTrend": 135.87,
                    "minorAxisLength": 30.0386,
                    "minorAxisTrend": 30.0386,
                    "depthUncertainty": -1.0,
                    "timeUncertainty": "PT8.069S"
                  }
                ],
                "ellipsoids": []
              },
              "featurePredictions": [],
              "locationBehaviors": [
                {
                  "residual": -3.29,
                  "weight": 0.558,
                  "defining": false,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "ed5545c2-6546-4b0e-b932-5493fd54e51e"
                },
                {
                  "residual": 0.3,
                  "weight": 0.534,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "d7c8eaf8-1a7f-4aac-92ad-d71d7463f419"
                },
                {
                  "residual": 0.0,
                  "weight": 0.534,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "78585520-cf4a-46df-aee0-1573f8c942ba"
                },
                {
                  "residual": -3.55,
                  "weight": 0.534,
                  "defining": false,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "667da06a-0041-4f7e-899d-8e35e3dc8b42"
                },
                {
                  "residual": -0.9,
                  "weight": 0.558,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "0dcc573c-ceb8-482b-83ce-8b82b4f074d7"
                },
                {
                  "residual": 0.0,
                  "weight": 0.558,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "3722684d-6e7e-4586-933d-8dd1d1a23af2"
                }
              ]
            }
          ],
          "preferredLocationSolution": {
            "locationSolution": {
              "id": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
              "location": {
                "latitudeDegrees": 45.27125,
                "longitudeDegrees": 130.55595,
                "depthKm": 0.0,
                "time": "2010-05-20T21:37:15.048Z"
              },
              "locationRestraints": {
                "depthRestraintType": "FIXED_AT_SURFACE",
                "depthRestraintKm": null,
                "positionRestraintType": "UNRESTRAINED",
                "latitudeRestraintDegrees": null,
                "longitudeRestraintDegrees": null,
                "timeRestraintType": "UNRESTRAINED",
                "timeRestraint": null
              },
              "locationUncertainty": {
                "xx": 316.5223,
                "xy": -124.0671,
                "xz": -1.0,
                "xt": 69.3975,
                "yy": 324.0427,
                "yz": -1.0,
                "yt": -71.5326,
                "zz": -1.0,
                "zt": -1.0,
                "tt": 24.0261,
                "stDevOneObservation": 0.0,
                "ellipses": [
                  {
                    "scalingFactorType": "CONFIDENCE",
                    "kWeight": 0.0,
                    "confidenceLevel": 0.9,
                    "majorAxisLength": 45.2134,
                    "majorAxisTrend": 135.87,
                    "minorAxisLength": 30.0386,
                    "minorAxisTrend": 30.0386,
                    "depthUncertainty": -1.0,
                    "timeUncertainty": "PT8.069S"
                  }
                ],
                "ellipsoids": []
              },
              "featurePredictions": [],
              "locationBehaviors": [
                {
                  "residual": -3.29,
                  "weight": 0.558,
                  "defining": false,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "ed5545c2-6546-4b0e-b932-5493fd54e51e"
                },
                {
                  "residual": 0.3,
                  "weight": 0.534,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "d7c8eaf8-1a7f-4aac-92ad-d71d7463f419"
                },
                {
                  "residual": 0.0,
                  "weight": 0.534,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "78585520-cf4a-46df-aee0-1573f8c942ba"
                },
                {
                  "residual": -3.55,
                  "weight": 0.534,
                  "defining": false,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "667da06a-0041-4f7e-899d-8e35e3dc8b42"
                },
                {
                  "residual": -0.9,
                  "weight": 0.558,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "0dcc573c-ceb8-482b-83ce-8b82b4f074d7"
                },
                {
                  "residual": 0.0,
                  "weight": 0.558,
                  "defining": true,
                  "featurePredictionId": "00000000-0000-0000-0000-000000000000",
                  "featureMeasurementId": "3722684d-6e7e-4586-933d-8dd1d1a23af2"
                }
              ]
            }
          },
          "associations": [
            {
              "id": "ec37f823-78c5-4893-a224-c805aa9f1cc0",
              "eventHypothesisId": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
              "signalDetectionHypothesisId": "b87787d0-68ce-38df-95f9-7dcb0b1f1fe4",
              "rejected": false
            },
            {
              "id": "d286d5a3-1cf4-479d-a080-b4987b4e8e9d",
              "eventHypothesisId": "f587793b-8c91-36bf-8b96-8d1b6f917efa",
              "signalDetectionHypothesisId": "fe43ddb1-9dc3-35ba-97e5-3d2e858e70a7",
              "rejected": false
            }
          ]
        }
      ],
      "finalEventHypothesisHistory": [
        {
          "eventHypothesisId": "f587793b-8c91-36bf-8b96-8d1b6f917efa"
        }
      ],
      "preferredEventHypothesisHistory": [
        {
          "processingStageId": "00000000-0000-0000-0000-000000000000",
          "eventHypothesisId": "f587793b-8c91-36bf-8b96-8d1b6f917efa"
        }
      ]
  }
]'
```

**Response**

```
{"storedEvents":["f587793b-8c91-36bf-8b96-8d1b6f917efa"],"updatedEvents":[],"errorEvents":[]}
```

## Configuraton

Two YAML files comprise the association control service configuration.  Both files discussed below are 
given relative to the directory:
```
gms/core/signal-detection/association/signal-detection-association-control-service/src/main/resources/gms/core/signaldetection/association/control/service/baseconfig
```

### Default Associator Default Parameters

The file, `signal-detection-association-control.default/associator-default.yaml`, contains the
associator default parameters.  Here is an example file:
```
name: "associator-default"
constraints:
  - constraintType: "DEFAULT"
parameters:
  pluginName: "globalGridSignalDetectionAssociatorPlugin"
  pluginVersion: "1.0.0"
  gridModelFileName: "geotess_grid_populated_geotess_grid_04000.geotess"
  gridSpacing: 4000
  maxStationsPerGrid: 5
  sigmaSlowness: 1.5
  phases: ['P']
  forwardTransformationPhases: ['P']
  beliefThreshold: 0.05
  primaryPhaseRequiredForSecondary: true
  sigmaTime: 5.0
  chiLimit: 0.75
  freezeArrivalsAtBeamPoints: true
  gridCylinderRadiusDegrees: 3.0
  gridCylinderDepthKm: 50
  gridCylinderHeightKm: 100
  minimumMagnitude: 3.5
  numFirstSta: 5
```

And the parameters are described as follows:

| Parameter Name                   | Type     | Description                               |
| :---                             | :---     | :---                                      |
| pluginName  	      	      	   | string   | name of the associator plugin             |
| pluginVersion                    | string   |	version	of the plugin                     |
| gridModelFileName                | string   | file containing GeoTess grid model        |
| gridSpacing                      | int      |                                           |
| maxStationsPerGrid               | int      |	                                          |
| sigmaSlowness                    | float    |                                           |
| phases                           | [string] |                                           |
| forwardTransformationPhases      | [string] |                                           |
| beliefThreshold                  | float    |                                           |
| primaryPhaseRequiredForSecondary | bool     |                                           |
| sigmaTime                        | float    |                                           |
| chiLimit                         | float    |                                           |
| freezeArrivalsAtBeamPoints       | bool     |                                           |
| gridCylinderRadiusDegrees        | float    |                                           |
| gridCylinderDepthKm              | int      |                                           |
| gridCylinderHeightKm             | int      |                                           |
| minimumMagnitude                 | float    |                                           |
| numFirstSta                      | int      |                                           |


### Weighted Event Plugin Parameters

The file, `signal-detection-association-control.quality-metrics.weighted-event/weighted-event-plugin.yaml`,
contains the associator default parameters.  Here is an example file:
```
name: "default-weighted-event-plugin"
constraints:
  - constraintType: "DEFAULT"
parameters:
  name: "defaultWeightedEventCriteriaCalculationPlugin"
  version: "1.0.0"
  primaryTimeWeight: 1.5
  secondaryTimeWeight: 0.65
  arrayAzimuthWeight: 0.5
  threeComponentAzimuthWeight: 0.5
  arraySlowWeight: 0.65
  threeComponentSlowWeight: 0.65
  weightThreshold: 0.77
```

And the parameters are described as follows:

| Parameter Name              | Type  | Description    |
| :---                        | :---  | :---           |
| primaryTimeWeight   	      | float |                |
| secondaryTimeWeight         | float |                |
| arrayAzimuthWeight          | float |                |
| threeComponentAzimuthWeight | float |                |
| arraySlowWeight             | float |                |
| threeComponentSlowWeight    | float |                |
| weightThreshold             | float |                |
