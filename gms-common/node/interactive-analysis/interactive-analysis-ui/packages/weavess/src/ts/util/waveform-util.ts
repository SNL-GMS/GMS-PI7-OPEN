/* tslint:disable:no-bitwise*/
import { fill } from 'lodash';
import * as Entities from '../entities';
import { getSecureRandomNumber } from './random-number-util';

/** Creates a UUID (version 4) */
export function UUIDv4(a?: number) {
  // tslint:disable-next-line:no-magic-numbers
  return a ? (a ^ getSecureRandomNumber() * 16 >> a / 4).toString(16) :
    // tslint:disable-next-line
    (([1e7] as any) + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, UUIDv4);
}

/**
 * Creates a flat line channel segment.
 * 
 * @param startTimeSecs the start time in seconds
 * @param endTimeSecs the endtime in seconds
 * @param amplitude the amplitude of the flat segment
 * @param color (optional) the color of the segment
 * @param sampleRate (optional) the sample rate (default 1 hz)
 * @param displayType (optional) the display type of the segment
 * @param pointSize (optional) the point size of the segment
 * @param description (optional) the description of the segment
 * @param descriptionLabelColor (optional) the description color of the segment
 */
export function createFlatLineChannelSegment(
  startTimeSecs: number,
  endTimeSecs: number,
  amplitude: number,
  color?: string,
  sampleRate?: number,
  displayType?: Entities.DisplayType[],
  pointSize?: number,
  description?: string,
  descriptionLabelColor?: string,
): Entities.ChannelSegment {

  return {
    description,
    descriptionLabelColor,
    dataSegments: [createFlatLineDataSegment(
      startTimeSecs, endTimeSecs, amplitude, color, sampleRate, displayType, pointSize)]
  };
}

/**
 * Creates a flat line data segment.
 * 
 * @param startTimeSecs the start time in seconds
 * @param endTimeSecs the endtime in seconds
 * @param amplitude the amplitude of the flat segment
 * @param color (optional) the color of the segment
 * @param sampleRate (optional) the sample rate (default 1 hz)
 * @param displayType (optional) the display type of the segment
 * @param pointSize (optional) the point size of the segment
 */
export function createFlatLineDataSegment(
  startTimeSecs: number,
  endTimeSecs: number,
  amplitude: number,
  color?: string,
  sampleRate: number = 1,
  displayType?: Entities.DisplayType[],
  pointSize?: number
): Entities.DataSegment {
  if (endTimeSecs <= startTimeSecs) {
    throw new Error('End time seconds must be greater than start time seconds');
  }

  if (sampleRate <= 0) {
    throw new Error('Sample Rate must be greater than zero');
  }

  const numberOfPoints = Math.floor((endTimeSecs - startTimeSecs) * sampleRate);

  const data: number[] = fill(Array(numberOfPoints), amplitude);
  const dataSegment: Entities.DataSegment = {
    startTimeSecs,
    sampleRate,
    color,
    displayType,
    pointSize,
    data
  };
  return dataSegment;
}

/**
 * Creates a dummy Station data
 * 
 * @param startTimeSecs start of the data, waveforms will start here
 * @param endTimeSecs end of the data, waveforms will end here
 * @param sampleRate how much data
 * @param eventAmplitude the const y value of the waveforms 
 * @param noiseAmplitude percentage that calculates and effects the waveforms amplitude
 * 
 * @returns StationConfig dummy data generated station config
 */
export function createDummyWaveform(
  startTimeSecs: number,
  endTimeSecs: number,
  sampleRate: number,
  eventAmplitude: number,
  noiseAmplitude: number): Entities.Station {

  let currentEventAmplitude = 0;
  let currentEventPeak = 0;
  let eventBuildup = 0;
  const data: any[] = [];
  const signalDetections: Entities.PickMarker[] = [];
  const theoreticalPhaseWindows: Entities.TheoreticalPhaseWindow[] = [];
  const theoreticalPhaseWindowColors = ['gold', 'plum', 'cyan'];

  const samples = (endTimeSecs - startTimeSecs) * sampleRate;
  for (let i = 1; i < samples; i++) {

    // tslint:disable-next-line:no-magic-numbers
    if (i % Math.round(samples / (getSecureRandomNumber() * 10)) === 0) {
      // tslint:disable-next-line:no-magic-numbers
      currentEventAmplitude = 0.05;
      currentEventPeak = getSecureRandomNumber() * eventAmplitude;
      eventBuildup = 1;
      signalDetections.push({
        // tslint:disable-next-line:newline-per-chained-call
        id: UUIDv4().toString(),
        color: 'red',
        label: 'P',
        // tslint:disable-next-line:no-magic-numbers
        timeSecs: startTimeSecs + 100,
      });
      theoreticalPhaseWindows.push({
        color: theoreticalPhaseWindowColors[Math.floor(getSecureRandomNumber() * theoreticalPhaseWindowColors.length)],
        // tslint:disable-next-line:newline-per-chained-call
        id: UUIDv4().toString(),
        label: 'P',
        // tslint:disable-next-line:no-magic-numbers
        startTimeSecs: startTimeSecs + 200,
        // tslint:disable-next-line:no-magic-numbers
        endTimeSecs: startTimeSecs + 300,
      });
    }
    if (currentEventAmplitude >= currentEventPeak) {
      eventBuildup = -1;
    }
    if (eventBuildup === 1) {
      // tslint:disable-next-line:no-magic-numbers
      currentEventAmplitude += currentEventAmplitude * (1 / samples) * 125;
    } else if (eventBuildup === -1) {
      // tslint:disable-next-line:no-magic-numbers
      currentEventAmplitude -= currentEventAmplitude * (1 / samples) * 62;
    }
    if (currentEventAmplitude < 0) {
      currentEventAmplitude = 0;
    }
    data.push(currentEventAmplitude + noiseAmplitude - getSecureRandomNumber() * noiseAmplitude * 2
      - getSecureRandomNumber() * currentEventAmplitude * 2);
  }

  return {
    // tslint:disable-next-line:newline-per-chained-call
    id: UUIDv4().toString(),
    name: 'dummy station',
    defaultChannel: {
      id: UUIDv4()
        .toString(),
      name: 'dummy channel',
      height: 75,
      waveform: {
        channelSegmentId: 'data',
        channelSegments: new Map([
          [
            'data',
            {
              description:
                `eventAmplitude: ${eventAmplitude.toFixed(2)}, noiseAmplitude: ${noiseAmplitude.toFixed(2)}`,
              dataSegments: [{
                data,
                sampleRate,
                startTimeSecs

              }]
            }
          ]
        ]),
        signalDetections,
        theoreticalPhaseWindows,
      }
    },
    nonDefaultChannels: [],
  };
}
