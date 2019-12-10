import { convertGraphicsXYtoCoordinate,
         convertPolarToXY,
         convertXYtoPolar,
         getSortedSignalDetections
        } from '~analyst-ui/components/azimuth-slowness/components/fk-util';
import { FkPowerSpectra } from '~graphql/fk/types';
import { SignalDetection } from '~graphql/signal-detection/types';
import { DistanceToSource } from '~graphql/station/types';
import { WaveformSortType } from '~state/analyst-workspace/types';
import { signalDetectionsSorted } from '../../../..//__data__/sds-with-fk-sorted';
import { distancesToSource } from '../../../../__data__/distance-to-source';
import { signalDetectionsData, signalDetectionsSortedByStationName } from '../../../../__data__/signal-detections-data';
import { signalDetectionsForTestingSort } from '../../../../__data__/signal-detections-to-test-sort';
// tslint:disable: no-magic-numbers

const fkSpectra: FkPowerSpectra = {
  id: undefined,
  contribChannels: [],
  startTime: undefined,
  sampleRate: undefined,
  sampleCount: undefined,
  windowLead: undefined,
  windowLength: undefined,
  stepSize: undefined,
  phaseType: undefined,
  lowFrequency: undefined,
  highFrequency: undefined,
  xSlowStart: -20,
  xSlowCount: undefined,
  xSlowDelta: undefined,
  ySlowStart: -20,
  ySlowCount: undefined,
  ySlowDelta: undefined,
  reviewed: undefined,
  leadSpectrum: undefined,
  fstatData: undefined,
  configuration: undefined
};
const incrementAmt = 10;
const canvasDimension = 40;
const sqrtOfFifty = 7.0710678118654755;

/**
 * Tests helper function that converts graphic space to xy coordinates
 */
describe('convertGraphicsXYtoCoordinate', () => {
  test('tests calling function with valid inputs and known output', () => {
    let xyValue = 0;

    let xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy.x)
      .toEqual(-20);
    expect(xy.y)
      .toEqual(20);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy.x)
      .toEqual(-10);
    expect(xy.y)
      .toEqual(10);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy.x)
      .toEqual(0);
    expect(xy.y)
      .toEqual(0);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy.x)
      .toEqual(10);
    expect(xy.y)
      .toEqual(-10);

    xyValue += incrementAmt;
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy.x)
      .toEqual(20);
    expect(xy.y)
      .toEqual(-20);
  });

  test('test bad input', () => {
    const xyValue = 0;
    let xy = convertGraphicsXYtoCoordinate(undefined, xyValue, fkSpectra, canvasDimension, canvasDimension);
    expect(xy)
      .toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, undefined, fkSpectra, canvasDimension, canvasDimension);
    expect(xy)
      .toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, undefined, canvasDimension, canvasDimension);
    expect(xy)
      .toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, undefined, canvasDimension);
    expect(xy)
      .toBeUndefined();
    xy = convertGraphicsXYtoCoordinate(xyValue, xyValue, fkSpectra, canvasDimension, undefined);
    expect(xy)
      .toBeUndefined();
  });
});

describe('convertXYtoPolar', () => {
  test('test valid inputs to xy to polar conversion', () => {
    const polar = convertXYtoPolar(5, 5);
    expect(polar.azimuthDeg)
        .toEqual(45);
    expect(polar.radialSlowness)
        .toEqual(sqrtOfFifty);
  });

  test('bad inputs for conversion',  () => {
    const polar = convertXYtoPolar(undefined, undefined);
    expect(polar.azimuthDeg)
      .toBeUndefined();
    expect(polar.radialSlowness)
      .toBeUndefined();
  });
});

describe('convertPolarToXY', () => {
  test('test valid inputs to polar to xy conversion', () => {
    const xy = convertPolarToXY(sqrtOfFifty, 45);
    expect(xy.x)
      .toBeCloseTo(5, 5);
    expect(xy.y)
        .toBeCloseTo(-5, 5);
  });

  test('bad inputs for conversion',  () => {
    const xy = convertPolarToXY(undefined, undefined);
    expect(xy.x)
      .toBeNaN();
    expect(xy.y)
      .toBeNaN();
  });
});

describe('getSortedSignalDetections', () => {

  it('test getSortedSignalDetections returns empty list with empty lists', () => {
      const emptySds: SignalDetection[] = [];
      const distanceSortType: WaveformSortType = WaveformSortType.distance;
      const emptyDistanceToSource: DistanceToSource[] = [];
      const sortedSds = getSortedSignalDetections(emptySds, distanceSortType, emptyDistanceToSource);
      expect(sortedSds)
      .toEqual([]);
  });
  it('test getSortedSignalDetections correctly sorts by station', () => {
    const sds: SignalDetection[] = signalDetectionsData;
    const distanceSortType: WaveformSortType = WaveformSortType.stationName;
    const emptyDistanceToSource: DistanceToSource[] = [];
    const sortedSds = getSortedSignalDetections(sds, distanceSortType, emptyDistanceToSource);
    expect(sortedSds)
    .toEqual(signalDetectionsSortedByStationName);
  });
// tslint:disable-next-line: max-line-length
  it('test getSortedSignalDetections correctly sorts by distance to source', () => {
    const sds: any[] = signalDetectionsForTestingSort;
    const distanceSortType: WaveformSortType = WaveformSortType.distance;
    const distances: any[] = distancesToSource;
    const sortedSds = getSortedSignalDetections(sds, distanceSortType, distances);

    expect(sortedSds.map(sd => sd.id))
    .toEqual(signalDetectionsSorted.map(sd => sd.id));
  });
});
