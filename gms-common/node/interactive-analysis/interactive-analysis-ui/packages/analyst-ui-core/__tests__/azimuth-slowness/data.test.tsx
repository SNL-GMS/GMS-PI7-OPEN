import { eventData } from '../__data__/event-data';
import { signalDetectionsData } from '../__data__/signal-detections-data';
import {
  endTimeSeconds,
  eventIds,
  signalDetectionsIds,
  startTimeSeconds,
  timeBlock,
} from '../__data__/test-util';

describe('Data should be verified', () => {
  describe('FK Data', () => {

    it('should be the correct time block', () => {
      expect(endTimeSeconds - startTimeSeconds)
        .toBeGreaterThanOrEqual(
        timeBlock
      );
    });

    it('TimeInterval is mocked properly', () => {
      expect(endTimeSeconds - startTimeSeconds)
        .toBeGreaterThanOrEqual(
        timeBlock
      );
    });

    it('should have valid signal detection data', () => {
      expect(signalDetectionsData)
        .toMatchSnapshot();
    });

    it('should have valid signalDetectionIds', () => {
      expect(signalDetectionsIds)
        .toMatchSnapshot();
    });

    it('should have valid event data', () => {
      expect(eventData)
        .toMatchSnapshot();
    });

    it('should have valid eventIds', () => {
      expect(eventIds)
        .toMatchSnapshot();
    });

  });
});
