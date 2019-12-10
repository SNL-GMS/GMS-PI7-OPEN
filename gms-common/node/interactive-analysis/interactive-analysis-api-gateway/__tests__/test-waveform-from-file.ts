// tslint:disable:max-line-length
import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
// No need for pre-test setup yet...
// beforeEach(async () => await setupTest());

// ---- Query test cases ----

// Test case - basic content query for waveforms
it('Waveform content query should match snapshot', async () => {
  // language=GraphQL

  // ChannelID comes from SIV/MH1, which has five segments specified
  // in `test_data/ueb/ueb_wfdisc.txt`.  The `test_data/ueb/SIV0.w`
  // file contains the first 32768 bytes of the `tonto2/GNEM/indexpool/proj
  // This is enough to read samples for all of the channel 5332 segments.
  // The file was truncated here to keep the Git repository smaller.
  // tslint:disable-next-line:no-console
  console.log('pre query');
  const query = `
  query getRawWaveformSegmentsByChannels {
    getRawWaveformSegmentsByChannels(timeRange: {startTime: 1274385600, endTime: 1274400000}, channelIds: ["channel1-1111-1111-1111-111111111110"]) {
      startTime
      endTime
      type
      channel {
        name
        site {
          name
          station {
            name
            networks {
              name
            }
          }
        }
      }
      timeseries {
        startTime
        sampleRate
        sampleCount
      }
      creationInfo{
         creatorId
        creatorType
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  // tslint:disable-next-line:no-console
  console.log('post query');

  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
