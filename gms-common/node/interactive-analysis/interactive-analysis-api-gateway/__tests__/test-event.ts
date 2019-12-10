// tslint:disable:max-line-length
import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
// import { readJsonData } from '../src/ts/util/file-parse';
// import { convertEventHypothesisToOSD, convertEventHypothesisFromOSD } from '../src/ts/util/event-utils';

// Setup
// const eventHypothesis: any = readJsonData('./resources/test_data/unit-test-data/event/event-hypothesis.json');
// const eventHypothesisOSD: any = readJsonData('./resources/test_data/unit-test-data/event/event-hypothesis-osd.json');
// const eventHypothesisConversionOsd = convertEventHypothesisToOSD(eventHypothesis);
// const eventHypothesisConversion = convertEventHypothesisFromOSD(eventHypothesisOSD);
// delete(eventHypothesis.preferredLocationSolution.creationInfo);
// delete(eventHypothesisOSD.preferredLocationSolution.creationInfo);
// delete(eventHypothesis.preferredLocationSolution.eventHypothesisConversionOsd);
// delete(eventHypothesis.preferredLocationSolution.eventHypothesisConversion);

// // Test that Convert methods in event utlis work as expected
// describe('When converting event hypothesis to an OSD event hypothesis and back', async () => {
//   it('The osd event hypothesis should match expected result', async () => {
//     expect(JSON.stringify(eventHypothesisOSD)).toEqual(JSON.stringify(eventHypothesisConversionOsd));
//   });
//   it('The event hypothesis should match expected result', async () => {
//     delete(eventHypothesisConversion.preferredLocationSolution.creationInfo);
//     expect(JSON.stringify(eventHypothesis)).toEqual(JSON.stringify(eventHypothesisConversion));
//   });
// });

// ---- Query test cases ---

// Test case - Query for event hypotheses in the time range
xit('Querying for event hypotheses by time range should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query eventHypothesesInTimeRange {
    eventHypothesesInTimeRange(timeRange: {startTime: 1, endTime: 9999999999}) {
      id
      rejected
      event {
        id
        monitoringOrganization
        hypotheses {
          id
        }
      }
      locationSolutionSets {
        latDegrees
        lonDegrees
        depthKm
        timeSec
        networkMagnitudeSolutions {
          magnitudeType
          magnitude
        }
      }
      preferredLocationSolution {
        locationSolution {
          latDegrees
          lonDegrees
          depthKm
          timeSec
        }
        creationInfo {
          creatorId
          creatorType
        }
      }
      signalDetectionAssociations {
        signalDetectionHypothesis {
          id
          arrivalTimeMeasurement {
            timeSec
          }
          signalDetectionAssociations {
            eventHypothesis {
              id
            }
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

// Test case - Query for a single event hypothesis by ID
xit('Querying for a single event hypothesis by ID should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query eventHypothesisById {
    eventHypothesisById(hypothesisId: "48835593"){
      id
      rejected
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

// Test case - Query for event hypotheses by ID
xit('Querying for event hypotheses by ID should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query eventHypothesesById {
    eventHypothesesById(hypothesisIds: ["48835593", "48835444"]){
      id
      rejected
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

// Test case - Create new event hypotheses (mutation)
xit('Creating new event hypotheses should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation createEventHypotheses{
    createEventHypotheses (eventIds: ["48835593"], input: { processingStageId: "1", locationSolutionInput: {latDegrees: 0, lonDegrees: 0, depthKm: 0, timeSec: 0 }, creatorId:"test" associatedSignalDetectionIds: ["59191572"]}){
      rejected
      event{
        id
      }
      locationSolutionSets{
        latDegrees
        lonDegrees
        depthKm
        timeSec
      }
      signalDetectionAssociations{
        signalDetectionHypothesis{
          id
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

// Test case - Update existing event hypotheses (mutation)
xit('Updating an existing event hypotheses should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateEventHypotheses {
    updateEventHypotheses(hypothesisIds: ["48835593"], input: {rejected: true, locationSolutionInput: {latDegrees: 1, lonDegrees: 1, depthKm: 1, timeSec: 1}, creatorId: "Test", processingStageId: "1"}) {
      id
      rejected
      preferredLocationSolution {
        locationSolution {
          latDegrees
          lonDegrees
          depthKm
          timeSec
        }
        creationInfo {
          creatorId
          creatorType
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

// Test case - Update existing events (mutation)
xit('Updating existing events should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation updateEvents {
    updateEvents(eventIds: ["48835593"], input: {creatorId: "Test", processingStageId: "1", status: OpenForRefinement, preferredHypothesisId: "48835593", activeAnalystUserNames: ["Ryan", "Mark"]}) {
      preferredHypothesis {
        hypothesis {
          id
        }
      }
      status
      activeAnalysts {
        userName
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
