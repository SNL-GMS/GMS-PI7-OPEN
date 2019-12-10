# Feature Prediction

## Description
This service allows the user to pass in an event location and set of receiver
locations and obtain feature predictions and their associated uncertainties for
a list of feature measurement types.  The currently supported feature measurment
types are arrival time, azimuth and back-azimuth, slowness, and mb corrections
and uncertainty.

## Project Structure

### Feature Prediction Service (`fp-service`)

#### Running `fp-service` locally
1. cd into `gms-common/java/gms/core/feature-prediction/fp-service`
2. run `gradle docker`
3. run `docker run -p 8080:8080 local/gms-common/fp-service`

`fp-service` should now be running on port 8080 on your machine

####Available Feature Measurement Prediction Types

* `ARRIVAL_TIME`
* `AZIMUTH`
* `SLOWNESS`
* `MAGNITUDE_CORRECTION`

#### Endpoints

* `/feature-measurement/prediction/for-source-and-receiver-locations`
* **Request Type:** `POST`
* **Request Body Example:**
```
{
  "featureMeasurementTypes": [
    {
      "featureMeasurementTypeName": "ARRIVAL_TIME"
    }
  ],
  "sourceLocation": {
    "latitudeDegrees": 10.0,
    "longitudeDegrees": 110.0,
    "depthKm": 70.0,
    "time": 0.0
  },
  "receiverLocations": [
    {
      "latitudeDegrees": -23.665134,
      "longitudeDegrees": 133.905261,
      "elevationKm": 0.6273,
      "depthKm": 0.0
    },
    {
      "latitudeDegrees": 46.7936830,
      "longitudeDegrees": 82.290569,
      "elevationKm": 0.6176,
      "depthKm": 0.0
    }
  ],
  "phase": "P",
  "model": "ak135",
  "correctionDefinitions": [
    {
      "correctionType": "ELLIPTICITY_CORRECTION"
    },
    {
      "correctionType": "ELEVATION_CORRECTION",
      "usingGlobalVelocity": "false"
    }
  ],
  "processingContext": {
    "analystActionReference": null,
    "processingStepReference": {
      "processingStageIntervalId": "65054172-557f-42ac-894f-941cf55ca5f2",
      "processingSequenceIntervalId": "67c8ef40-d6b4-41d3-a825-733cc22391be",
      "processingStepId": "0817a095-4a1f-4d8f-8897-3d55777c70b5"
    },
    "storageVisibility": "PUBLIC"
  }
}'
```
* **Response Body Example**
```
[
  {
    "id": "69f515c8-4a7e-4346-80b2-3dc4f5c2f9ed",
    "phase": "P",
    "predictedValue": {
      "value": "1970-01-01T00:07:36.325331459Z",
      "standardDeviation": "PT1.16S"
    },
    "featurePredictionComponents": [
      {
        "value": {
          "value": 0.09772205078071018,
          "standardDeviation": 0.0,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "ELEVATION_CORRECTION"
      },
      {
        "value": {
          "value": 455.931069757,
          "standardDeviation": 1.16,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "BASELINE_PREDICTION"
      },
      {
        "value": {
          "value": 0.2965396515425364,
          "standardDeviation": 0.0,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "ELLIPTICITY_CORRECTION"
      }
    ],
    "extrapolated": false,
    "sourceLocation": {
      "latitudeDegrees": 10.0,
      "longitudeDegrees": 110.0,
      "depthKm": 70.0,
      "time": "1970-01-01T00:00:00Z"
    },
    "receiverLocation": {
      "latitudeDegrees": -23.665134,
      "longitudeDegrees": 133.905261,
      "depthKm": 0.0,
      "elevationKm": 0.6273
    },
    "channelId": null,
    "featurePredictionDerivativeMap": {
      "D_DZ": {
        "value": -0.0993607877108765,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DT": {
        "value": 1.0,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DX": {
        "value": -4.64925472737549,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DY": {
        "value": 6.773095397867868,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      }
    },
    "predictionType": "ARRIVAL_TIME"
  },
  {
    "id": "0e731c4f-bdbf-4c78-9a75-825bc73caec6",
    "phase": "P",
    "predictedValue": {
      "value": "1970-01-01T00:07:57.785784934Z",
      "standardDeviation": "PT1.16S"
    },
    "featurePredictionComponents": [
      {
        "value": {
          "value": 0.09669606018137547,
          "standardDeviation": 0.0,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "ELEVATION_CORRECTION"
      },
      {
        "value": {
          "value": 477.665364346,
          "standardDeviation": 1.16,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "BASELINE_PREDICTION"
      },
      {
        "value": {
          "value": 0.02372452840677791,
          "standardDeviation": 0.0,
          "units": "SECONDS"
        },
        "extrapolated": false,
        "predictionComponentType": "ELLIPTICITY_CORRECTION"
      }
    ],
    "extrapolated": false,
    "sourceLocation": {
      "latitudeDegrees": 10.0,
      "longitudeDegrees": 110.0,
      "depthKm": 70.0,
      "time": "1970-01-01T00:00:00Z"
    },
    "receiverLocation": {
      "latitudeDegrees": 46.793683,
      "longitudeDegrees": 82.290569,
      "depthKm": 0.0,
      "elevationKm": 0.6176
    },
    "channelId": null,
    "featurePredictionDerivativeMap": {
      "D_DZ": {
        "value": -0.10060619384915323,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DT": {
        "value": 1.0,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DX": {
        "value": 3.7022119496127393,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      },
      "D_DY": {
        "value": -7.123946574380185,
        "standardDeviation": 0.0,
        "units": "UNITLESS"
      }
    },
    "predictionType": "ARRIVAL_TIME"
  }
]
```
* `/feature-measurement/prediction/for-location-solution-and-channel`
* **Request Type:** `POST`
* **Request Body Example:**
```
{
  "featureMeasurementTypes": [
    "ARRIVAL_TIME",
    "SLOWNESS"
  ],
  "sourceLocation": {
    "id": "713d7fb7-628d-4ed8-977f-e47559302776",
    "location": {
      "latitudeDegrees": 90.0,
      "longitudeDegrees": 80.0,
      "depthKm": 701.0,
      "time": "1970-01-01T00:00:00Z"
    },
    "locationRestraint": {
      "latitudeRestraintType": "UNRESTRAINED",
      "latitudeRestraintDegrees": null,
      "longitudeRestraintType": "UNRESTRAINED",
      "longitudeRestraintDegrees": null,
      "depthRestraintType": "UNRESTRAINED",
      "depthRestraintKm": null,
      "timeRestraintType": "UNRESTRAINED",
      "timeRestraint": null
    },
    "locationUncertainty": null,
    "featurePredictions": [],
    "locationBehaviors": []
  },
  "receiverLocations": [
    {
      "id": "84f0bc49-522a-49b5-9ecd-e014482b8a21",
      "name": "x",
      "channelType": "HIGH_BROADBAND_HIGH_GAIN_VERTICAL",
      "dataType": "HYDROACOUSTIC_ARRAY",
      "latitude": 1.1,
      "longitude": 1.2,
      "elevation": 1.3,
      "depth": 1.4,
      "verticalAngle": 1.5,
      "horizontalAngle": 1.6,
      "sampleRate": 1.7
    }
  ],
  "phase": "P",
  "model": "ak135",
  "processingContext": {
    "analystActionReference": {
      "processingStageIntervalId": "349a98fc-da1c-4706-9cac-b8e4f43a9761",
      "processingActivityIntervalId": "c0588834-1de0-48ed-b392-57f982cb3098",
      "analystId": "1aa97ca4-be19-402e-9fcb-e16ec51c2be6"
    },
    "processingStepReference": null,
    "storageVisibility": "PRIVATE"
  },
  "correctionDefinitions": []
}
```
* **Response Body Example**
```
{
  "id": "713d7fb7-628d-4ed8-977f-e47559302776",
  "location": {
    "latitudeDegrees": 90.0,
    "longitudeDegrees": 80.0,
    "depthKm": 701.0,
    "time": "1970-01-01T00:00:00Z"
  },
  "locationRestraint": {
    "latitudeRestraintType": "UNRESTRAINED",
    "latitudeRestraintDegrees": null,
    "longitudeRestraintType": "UNRESTRAINED",
    "longitudeRestraintDegrees": null,
    "depthRestraintType": "UNRESTRAINED",
    "depthRestraintKm": null,
    "timeRestraintType": "UNRESTRAINED",
    "timeRestraint": null
  },
  "locationUncertainty": null,
  "featurePredictions": [
    {
      "id": "e0618136-9316-4355-9e84-f911f91f6788",
      "phase": "P",
      "predictedValue": {
        "referenceTime": "2019-05-06T20:13:29.045824Z",
        "measurementValue": {
          "value": 4.628673354074075,
          "standardDeviation": 2.5,
          "units": "SECONDS_PER_DEGREE"
        }
      },
      "featurePredictionComponents": [
        {
          "value": {
            "value": 4.628673354074075,
            "standardDeviation": 2.5,
            "units": "SECONDS_PER_DEGREE"
          },
          "extrapolated": false,
          "predictionComponentType": "BASELINE_PREDICTION"
        }
      ],
      "extrapolated": false,
      "sourceLocation": {
        "latitudeDegrees": 90.0,
        "longitudeDegrees": 80.0,
        "depthKm": 701.0,
        "time": "1970-01-01T00:00:00Z"
      },
      "receiverLocation": {
        "latitudeDegrees": 1.1,
        "longitudeDegrees": 1.2,
        "depthKm": 1.4,
        "elevationKm": 1.3
      },
      "channelId": "84f0bc49-522a-49b5-9ecd-e014482b8a21",
      "featurePredictionDerivativeMap": {
        "D_DZ": {
          "value": -1.0250911230968995E-4,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DT": {
          "value": 0.0,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DX": {
          "value": -0.015246251432221643,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DY": {
          "value": -0.0030188390767159922,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        }
      },
      "predictionType": "SLOWNESS"
    },
    {
      "id": "eedc18c1-8854-44fc-9ae2-4e7ac6011515",
      "phase": "P",
      "predictedValue": {
        "value": "1970-01-01T00:11:42.967558610Z",
        "standardDeviation": "PT1.16S"
      },
      "featurePredictionComponents": [
        {
          "value": {
            "value": 702.96755861,
            "standardDeviation": 1.16,
            "units": "SECONDS"
          },
          "extrapolated": false,
          "predictionComponentType": "BASELINE_PREDICTION"
        }
      ],
      "extrapolated": false,
      "sourceLocation": {
        "latitudeDegrees": 90.0,
        "longitudeDegrees": 80.0,
        "depthKm": 701.0,
        "time": "1970-01-01T00:00:00Z"
      },
      "receiverLocation": {
        "latitudeDegrees": 1.1,
        "longitudeDegrees": 1.2,
        "depthKm": 1.4,
        "elevationKm": 1.3
      },
      "channelId": "84f0bc49-522a-49b5-9ecd-e014482b8a21",
      "featurePredictionDerivativeMap": {
        "D_DZ": {
          "value": -0.0829401114969698,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DT": {
          "value": 1.0,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DX": {
          "value": 4.540520989106398,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        },
        "D_DY": {
          "value": 0.8990473659377517,
          "standardDeviation": 0.0,
          "units": "UNITLESS"
        }
      },
      "predictionType": "ARRIVAL_TIME"
    }
  ],
  "locationBehaviors": []
}
```

### Configuration

General configuration is found in:
`gms/core/feature-prediction/fp-plugin-signalfeaturepredictor/src/main/resources/gms/core/featureprediction/plugins/implementations/signalfeaturepredictor/application.properties`
It consists of four `parameterName=paramterValue` lines:

| Parameter Name                      | Type             | Description                                                                                            |
| :---                                | :---             | :---                                                                                                   |
| earthmodels                         | CSV strings [^1] | list of earth models.  E.g., ak135,iasp91                                                              |
| attenuationEarthModels              | CSV strings      | list of attenuation models for magnitude correction                                                    |
| invalidPhasesForElevationCorrection | CSV strings      | list of phases for which travel time elevation correction is unavailable. E.g., L,LQ,G,H               |
| extrapolatetraveltimes              | bool             | true if system should extrapolate outside of table values                                              |
[^1] CSV = comma separated values

#### Travel Time Earth Models

Travel time earth models are in the directory:
`gms/core/feature-prediction/fp-plugin-standardasciiformat-earthmodel1d/src/main/resources`
Each model consists of a directory of files named after the phase they represent.  For example,
this directory contains a subdirectory named `earthmodels1d.ak135` which contains files named `P`,
`Lg`, `nNL`, etc.  Each of these files contains the travel time tables for the named phase.

The directory,
`gms/core/feature-prediction/fp-plugin-dziewonskigilbert-ellipticitycorrection/src/main/resources/gms/core/featureprediction/plugins/implementations/ellipticitycorrection`,
contains tables for travel time ellipticity correction.  For example, it contains the file, 
`ak135/ELCOR.dat` for the ak135 earth model.

#### Magnitude Correction

`gms/core/feature-prediction/fp-plugin-standardasciiformat-attenuationmodel1d/src/main/resources/attenuationModels`
contains attenuation models for magnitude correction.  In this directory, for example, is 
`VeithClawson72/P` which is the table of Q-factors for the `P` phase 
according the Veith-Clawson (1972) model.

`gms/core/feature-prediction/fp-plugin-standardasciiformat-attenuationmodel1d/src/main/resources/attenuationUncertaintyModels`
contains the uncertainty tables for attenuation.  For example, this directory contains the file,
`VeithClawson72/P`, which is the Murphy-Baker uncertainties for P waves.

#### Slowness

Slowness uncertainty tables are found in
`gms/core/feature-prediction/fp-plugin-signalfeaturepredictor/src/main/resources/gms/core/featureprediction/plugins/implementations/signalfeaturepredictor/slownessuncertainties`.
A subdirectory named for the earth model contains files named after the phases they model.  For
example, there is a file, `ak135/P` which contains a slowness uncertainty table for the P phase in 
the ak135 earth model.