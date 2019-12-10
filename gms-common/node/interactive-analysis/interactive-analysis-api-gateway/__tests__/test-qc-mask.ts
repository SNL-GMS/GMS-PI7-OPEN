// tslint:disable:max-line-length
import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
import { delayExecution } from '../src/ts/util/delay-execution';

const delayMs = 500;

// ---- Query test cases ----

// Test case - basic content query for waveforms
// Query for channel AS01/SHZ
it('QC Content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query qcmasksbychannel {
    channelsById(ids: ["channel1-1111-1111-1111-111111111110"]) {
      qcMasks(timeRange: {startTime: 1274392801, endTime: 1274396401}) {
        currentVersion {
          type
          category
          startTime
          endTime
        }
      }
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

// eb1f82bb-ed5c-4f6b-873c-0b9515aef272

// ---- Mutation test cases ----

// Test case - basic content query for waveforms
// Query for channel AS01/SHZ
it('QC Create mutation should match snapshot', async () => {
  // language=GraphQL
  const query = `
  mutation createMask {
    createQcMask(channelIds: ["channel1-1111-1111-1111-111111111110"], input: {timeRange: {startTime: 1274393921, endTime: 1274393926}, category: "ANALYST_DEFINED", type: "SPIKE", rationale: "testing"}) {
      channelId
      currentVersion {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
      qcMaskVersions {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
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

// Test case - basic content query for waveforms
// Query for channel AS01/SHZ
it('QC Update mutation should match snapshot', async () => {
  // language=GraphQL
  // mask Id corresponds a mask on channel AS01/SHZ
  const query = `
  mutation updateMask {
    updateQcMask(qcMaskId: "qcmask11-1111-1111-1111-111111111110", input: {timeRange: {startTime: 1274393921, endTime: 1274393936}, category: "ANALYST_DEFINED", type: "SPIKE", rationale: "Updating Test"}) {
      id
      channelId
      currentVersion {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
      qcMaskVersions {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
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

// Test case - basic content query for waveforms
// Query for channel AS01/SHZ
it('QC Create reject should match snapshot', async () => {
  // language=GraphQL
  // mask Id corresponds a mask on channel AS01/SHZ
  const query = `
  mutation rejectMask {
    rejectQcMask(qcMaskId: "qcmask11-1111-1111-1111-111111111110", rationale: "This was a crappy mask anyway") {
      id
      channelId
      currentVersion {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
      qcMaskVersions {
        startTime
        endTime
        category
        type
        rationale
        version
        channelSegmentIds
      }
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
