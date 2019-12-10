import { graphql } from 'graphql';
import { schema } from './src/ts/schema';

// No need for pre-test setup yet...
// beforeEach(async () => await setupTest());

// ---- Query test cases ----

// Test case - query waveforms for the default channel list
it('Default channel waveform list query results should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query defaultWaveformChannelSegments {
    defaultWaveformChannelSegments(timeRange: { startTime: 1274317202, endTime: 1274317204 }) {
      segmentType
      timeseries {
        startTime
        ... on Waveform {
          calibration {
            factor
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