// tslint:disable:max-line-length
import { graphql } from 'graphql';
import gql from 'graphql-tag';
import { schema } from '../src/ts/schema';
import { signalDetectionProcessor } from '../src/ts/signal-detection/signal-detection-processor';
import { TimeRange } from '../src/ts/common/model';
import { signalDetectionFragment } from '../gqls/signal-detection-gqls';
// No need for pre-test setup yet...
beforeEach(async () => await setupTest());
let timeRange: TimeRange;
let stationIds: string[];
async function setupTest() {
  timeRange = {
    startTime: 1274385600,
    endTime: 1274400000
  };
  stationIds = ['station1-1111-1111-1111-111111111111'];
  await signalDetectionProcessor.loadSignalDetections(timeRange, stationIds);
}

// Test case - Query for signal detection hypothesis by station ID and time range
it('Querying for signal detection hypothesis by station and time range should match snapshot', async () => {
  // language=GraphQL
  // Query for station SIV
  const signalDetectionsByStationQuery = gql`
  query signalDetectionsByStation($stationIds: [String]!, $timeRange: TimeRange!) {
    signalDetectionsByStation(stationIds: $stationIds, timeRange: $timeRange) {
      ...SignalDetectionFragment
    }
  }
  ${signalDetectionFragment}
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, signalDetectionsByStationQuery, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Query for signal detections by station ID and time range; show hypothesis IDs
it('Querying for signal detections by station and time range should match snapshot', async () => {
  const query = `
  query signalDetectionsByStation {
    signalDetectionsByStation(stationIds: ["station1-1111-1111-1111-111111111111"], timeRange: {startTime: 1274385600, endTime: 1274400000}) {
      id
      currentHypothesis {
        id
        rejected
        featureMeasurements {
          id
          featureMeasurementType
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Query for signal detection hypothesis by ID with Az/Slow and FK data
// This id comes from TXAR P associated to our favorite event
xit('Querying for signal detection hypothesis by ID with valid Az/Slow & FK data should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query azSlowFromSignalDetectionHypotheses {
    signalDetectionHypothesesById(hypothesisIds: ["d34e6262-7596-4f62-a8b6-6def4c065e65"]) {
      azSlownessMeasurement{
        azimuthDefiningRules{
          operationType
          isDefining
        }
        slownessDefiningRules{
          operationType
          isDefining
        }
        azimuthDeg
        slownessSecPerDeg
        azimuthUncertainty
        slownessUncertainty
        fkData{
          frequencyBand{
            minFrequencyHz
            maxFrequencyHz
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Query for signal detection by ID
it('Querying for signal detection by ID should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query signalDetectionsById {
    signalDetectionsById(detectionIds: ["sd111111-1111-1111-1111-111111111110", "sd111111-1111-1111-1111-111111111111"]) {
      id
      currentHypothesis {
        id
        rejected
        featureMeasurements {
          id
          featureMeasurementType
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// ---- Mutation test cases ----

// Test case - Create a new signal detection (mutation)
it('Creating a new detection should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation createDetection {
    createDetection(
      input: {
        stationId: "station1-1111-1111-1111-111111111111"
        phase: "P"
        signalDetectionTiming: {
          arrivalTime: 1
          timeUncertaintySec: 0
          amplitudeMeasurement: { startTime: 2, period: 1, amplitude: {value: 2, standardDeviation:0.1, units: UNITLESS} }
        }
      }
    ) {
    	sds {
        currentHypothesis {
          featureMeasurements {
            featureMeasurementType
            measurementValue{
              ...on PhaseTypeMeasurementValue {
                phase
              }
            }
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Update an existing signal detection phase (mutation)
it('Updating an existing detection phase should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updatePhase {
    updateDetection(detectionId: "sd111111-1111-1111-1111-111111111110", input: {phase: "S"}) {
      sds {
      currentHypothesis {
        featureMeasurements {
          featureMeasurementType
          measurementValue{
            ...on PhaseTypeMeasurementValue {
              phase
            }
        	}}
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Update an existing signal detection time (mutation)
it('Updating an existing detection time should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateTime {
    updateDetection(detectionId: "sd111111-1111-1111-1111-111111111110", input: {time: 1274324501, timeUncertaintySec: 0.1}) {
      sd {
        currentHypothesis {
          featureMeasurements {
            featureMeasurementType
            measurementValue{
              ...on InstantMeasurementValue {
                value
                standardDeviation
              }
            }
          }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Update an existing signal detection time & phase (mutation)
it('Updating an existing detection time & phase should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateTimeAndPhase {
    updateDetection(
      detectionId: "sd111111-1111-1111-1111-111111111110"
      input: {
        phase: "S"
        signalDetectionTiming: {
          arrivalTime: 1274324502
          timeUncertaintySec: 0.1
          amplitudeMeasurement: {
            startTime: 2
            period: 1
            amplitude: { value: 2, standardDeviation: 0.1, units: UNITLESS }
          }
        }
      }
    ) {
      sds {
        currentHypothesis {
          featureMeasurements {
            featureMeasurementType
            measurementValue {
              ... on InstantMeasurementValue {
                value
                standardDeviation
              }
              ... on PhaseTypeMeasurementValue {
                phase
              }
              ... on AmplitudeMeasurementValue {
                startTime
                period
                amplitude {
                  value
                  standardDeviation
                  units
                }
              }
            }
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Update a collection of existing signal detections with new time & phase (mutation)
it('Updating a collection of detections for time & phase should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateMultiTimeAndPhase {
    updateDetections(
      detectionIds: [
        "sd111111-1111-1111-1111-111111111111"
        "sd111111-1111-1111-1111-111111111112"
      ]
      input: {
        phase: "S"
        signalDetectionTiming: {
          arrivalTime: 1274324502
          timeUncertaintySec: 0.1
          amplitudeMeasurement: {
            startTime: 2
            period: 1
            amplitude: { value: 2, standardDeviation: 0.1, units: UNITLESS }
          }
        }
      }
    ) {
      sds{
        currentHypothesis {
          featureMeasurements {
            featureMeasurementType
            measurementValue {
              ... on InstantMeasurementValue {
                value
                standardDeviation
              }
              ... on PhaseTypeMeasurementValue {
                phase
              }
              ... on AmplitudeMeasurementValue {
                startTime
                period
                amplitude {
                  value
                  standardDeviation
                  units
                }
              }
            }
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Reject a collection of existing signal detection hypotheses
it('Rejecting a collection of detection hypotheses should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation rejectDetection {
    rejectDetections(detectionIds: ["sd111111-1111-1111-1111-111111111111", "sd111111-1111-1111-1111-111111111112"]) {
      sds {
        currentHypothesis {
          rejected
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Update a signal detection hypothesis az/slow measurement's FK
xit('Updating a signal detection hypothesis az/slow measurement\'s FK should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateAzSlowFromFk {
    updateAzSlowFromFk(sdHypothesisId: "4c062a8d-d0bc-430c-af13-574d34f29383",
      fkDataInput: {
        id: "e08319a4-bd6f-4ace-88c4-cd8e9190bf30",
        frequencyBand: {
          minFrequencyHz: 1.1,
          maxFrequencyHz: 2.1
        },
        slownessScale: {
          maxValue: 0.35932819,
          scaleValueCount: 1,
          scaleValues: [0.35932819]
        },
        windowParams: {
          windowType: "hanning",
          leadSeconds: 1.1,
          lengthSeconds: 4.4
        },
        attenuation: 40,
        contribChannelIds: [],
        peak: {xSlowness: -0.0359328,
          ySlowness: 0.0158037,
          azimuthDeg: 291.614,
          azimuthUncertainty: 0.1,
          radialSlowness: 0.0429026,
      		slownessUncertainty:0.1
          fstat: 0.1
        },
        theoretical: {
          xSlowness: -0.0359328,
          ySlowness: 0.0158037,
          azimuthDeg: 291.614,
          azimuthUncertainty: 0.1,
          radialSlowness: 0.0429026
          slownessUncertainty: 0.1,
          fstat: 0.1,
        },
        fkGrid: [[0.1]],
        fstatData: {
          azimuthWf: {
            id: "1",
            startTime: 0,
            endTime: 1,
            sampleRate: 4,
            sampleCount: 4,
            values: [0, 1, 2, 3]
          },
          slownessWf: {
            id: "1",
            startTime: 0,
            endTime: 1,
            sampleRate: 4,
            sampleCount: 4,
            values: [0, 1, 2, 3]
          },
          fstatWf: {
            id: "1",
            startTime: 0,
            endTime: 1,
            sampleRate: 4,
            sampleCount: 4,
            values: [0, 1, 2, 3]
          },
          beamWf: {
            id: "1",
            startTime: 0,
            endTime: 1,
            sampleRate: 4,
            sampleCount: 4,
            values: [0, 1, 2, 3]
          }
        }
      }
    ) {
      id
      azSlownessMeasurement {
        featureType
        azimuthDefiningRules{
          operationType
          isDefining
        }
        slownessDefiningRules{
          operationType
          isDefining
        }
        azimuthDeg
        slownessSecPerDeg
        fkData {
          id
          frequencyBand {
            minFrequencyHz
            maxFrequencyHz
          }
          slownessScale {
            maxValue
            scaleValueCount
            scaleValues
          }
          windowParams {
            windowType
            leadSeconds
            lengthSeconds
          }
          attenuation
          contribChannels {
            id
          }
          peak {
            xSlowness
            ySlowness
            azimuthDeg
            azimuthUncertainty
            fstat
            radialSlowness
            slownessUncertainty
          }
          theoretical {
            xSlowness
            ySlowness
            azimuthDeg
            azimuthUncertainty
            fstat
            radialSlowness
            slownessUncertainty
          }
          fkGrid
          fstatData {
            azimuthWf {
              id
              startTime
              endTime
              sampleRate
              sampleCount
              values
            }
            slownessWf {
              id
              startTime
              endTime
              sampleRate
              sampleCount
              values
            }
            fstatWf {
              id
              startTime
              endTime
              sampleRate
              sampleCount
              values
            }
            beamWf {
              id
              startTime
              endTime
              sampleRate
              sampleCount
              values
            }
          }
        }
      }
    }
  }`;

  // Execute the GraphQL mutation
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
