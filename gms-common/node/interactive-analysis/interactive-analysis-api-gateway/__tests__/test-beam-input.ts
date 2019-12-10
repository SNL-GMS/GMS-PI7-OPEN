import { ChannelSegmentProcessor } from '../src/ts/channel-segment/channel-segment-processor';
import { SignalDetection } from '../src/ts/signal-detection/model';
import { signalDetectionProcessor } from '../src/ts/signal-detection/signal-detection-processor';
import { TimeRange } from '../src/ts/common/model';
import { isOSDWaveformChannelSegment, OSDChannelSegment } from '../src/ts/channel-segment/model';
import { OSDWaveform } from '../src/ts/waveform/model';

// No need for pre-test setup yet...
// beforeEach(async () => await setupTest());
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

// ---- Method call to Channel Segment Processor ----
// Test case - Create Beam Input Parameter to send computeBeam streaming call
it('Creating new Beam Input Parameter should match snapshot', async () => {
  const sd: SignalDetection = signalDetectionProcessor.
    getSignalDetectionById('sd111111-1111-1111-1111-111111111110');
  const beamInput = await ChannelSegmentProcessor.Instance().buildBeamInput(sd);
  // Clear out the creationTime since that is current time
  if (isOSDWaveformChannelSegment(beamInput.waveforms[0])) {
    const osdWaveform = beamInput.waveforms[0] as OSDChannelSegment<OSDWaveform>;
    osdWaveform.creationInfo.creationTime = '2010-05-20T20:40:04.350Z';

    // TODO: Not sure why waveform data is coming back differently each time need
    // TODO: to figure out.
    osdWaveform.timeseries[0].values = [];
  }

  // Compare response to snapshot
  expect(beamInput).toMatchSnapshot();
});

// tslint:disable-next-line:no-empty
afterAll(async () => {});
