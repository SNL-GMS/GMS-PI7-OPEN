
// tslint:disable:max-line-length
import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
import { readJsonData } from '../src/ts/util/file-parse';
import { stationProcessor } from '../src/ts/station/station-processor';
// ---- Query test cases ----

// Setup
let stationResponse: any = readJsonData('./resources/test_data/unit-test-data/station/station-response.json');
let processingStation: any = readJsonData('./resources/test_data/unit-test-data/station/processing-station.json');
const dataAcquisition: any = 'dataAcquisition';

// Test that 3 methods in station processor work as expected
// processStationData also calls processSiteData, which called processChannelData
// so this test passing, ensures all three are working as expected
describe('When Processing a OSD Station response', async () => {
  it('the procesing station should match expected result', async () => {
    // dataAcquisition field values in processingStation are randomly generated, thus need to be set to empty
    processingStation = processingStation[dataAcquisition] = {};
    stationResponse = stationProcessor.processStationData(stationResponse).station[dataAcquisition] = {};
    expect(JSON.stringify(processingStation)).toEqual(JSON.stringify(stationResponse));
  });
});

// Test case - query default stations with rich information
it('Single channel query results should match snapshot', async () => {
  // language=GraphQL
  const query = `
    query defaultStations {
      defaultStations {
        name
        stationType
        location{
          latDegrees
          lonDegrees
          elevationKm
        }
        networks{
          name
          monitoringOrganization
        }
      }
    }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  // Execution the GraphQL query with a small delay to allow the API gateway to settle async
  // HTTP requests fetching data from the mock backend
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query a single channel and retrieve rich information
it('Single channel query results should match snapshot', async () => {
  // language=GraphQL
  // channel IDs correspond to channels: "AS01/SHZ", "AS02/SHZ"
  const query = `
  query channel{
    channelsById(ids: ["channel1-1111-1111-1111-111111111110"]){
      name
      channelType
      locationCode
      verticalAngle
      horizontalAngle
      sampleRate
      depth
      site{
        name
        location{
          latDegrees
          lonDegrees
          elevationKm
        }
        channels{
          name
        }
        station{
          name
          stationType
          location{
            latDegrees
            lonDegrees
            elevationKm
          }
          sites{
            name
          }
          networks{
            name
            monitoringOrganization
            stations{
              name
            }
          }
        }
      }
    }
  }
  `;

  const rootValue = {};
  // Execution the GraphQL query with a small delay to allow the API gateway to settle async
  // HTTP requests fetching data from the mock backend
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
