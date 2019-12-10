import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
import { delayExecution } from '../src/ts/util/delay-execution';

const delayMs = 500;

// ---- Query test cases ----

// Test case - basic content query for data acquisition
// Query for transferred files by time range
it('Transferred Files Content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query transferredFilesByTimeRange {
    transferredFilesByTimeRange(timeRange: {startTime: 0, endTime: 9999999999}) {
      stationName
      channelNames
      startTime
      endTime
      duration
      location
      priority
    }
  }
  `;

  const rootValue = {};
  // Execution the GraphQL query with a small delay to allow the API gateway to settle async
  // HTTP requests fetching data from the mock backend
  const result = await delayExecution(() => graphql(schema, query, rootValue), delayMs);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
