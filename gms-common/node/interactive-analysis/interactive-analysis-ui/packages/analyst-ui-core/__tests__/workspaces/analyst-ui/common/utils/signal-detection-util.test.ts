// tslint:disable
import { findMinMaxAmplitudeForPeakTrough,
         isPeakTroughInWarning,
         determineMinMaxForPeakTroughForWaveform,
         scaleAmplitudeForPeakTrough,
         calculateAmplitudeMeasurementValue,
         getWaveformValueForTime,
         scaleAmplitudeMeasurementValue
} from '~analyst-ui/common/utils/signal-detection-util';
import { systemConfig } from '~analyst-ui/config';
import { WaveformTypes } from '~graphql/';
import { FREQUENCY_VALUES, AMPLITUDE_VALUES } from '~analyst-ui/common/utils/amplitude-scale-constants';
import { AmplitudeMeasurementValue } from '~graphql/signal-detection/types';
import { Units } from '~graphql/common/types';

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('isPeakTroughInWarning', () => {
  const min = systemConfig.measurementMode.peakTroughSelection.warning.min;
  const max = systemConfig.measurementMode.peakTroughSelection.warning.max;
  const mid = ((max - min) / 2  + min);
  const startTimeOffsetFromSignalDetection = systemConfig.measurementMode.selection.startTimeOffsetFromSignalDetection;
  const endTimeOffsetFromSignalDetection = systemConfig.measurementMode.selection.endTimeOffsetFromSignalDetection;

  test('check [min] period value', () => {
    expect(isPeakTroughInWarning(
      0, min, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection)).toEqual(false);
  });

  test('check [max] period value', () => {
    expect(isPeakTroughInWarning(
      0, max, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection)).toEqual(false);
  });

  test('check [min, max] period value', () => {
    expect(isPeakTroughInWarning(
      0, mid, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection)).toEqual(false);
  });

  test('check bad [min] period value', () => {
    expect(isPeakTroughInWarning(
      0, min - 0.1, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection)).toEqual(true);
  });

  test('check bad [max] period value', () => {
    expect(isPeakTroughInWarning(
      0, max + 0.1, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection)).toEqual(true);
  });

  test('check trough greater than peak', () => {
    expect(isPeakTroughInWarning(
      0, mid, endTimeOffsetFromSignalDetection, 
      startTimeOffsetFromSignalDetection)).toEqual(true);
  });

  test('check trough out of range', () => {
    expect(isPeakTroughInWarning(
      0, mid, startTimeOffsetFromSignalDetection - 0.1, 
      endTimeOffsetFromSignalDetection)).toEqual(true);
  });

  test('check peak out of range', () => {
    expect(isPeakTroughInWarning(
      0, mid, startTimeOffsetFromSignalDetection, 
      endTimeOffsetFromSignalDetection + 0.1)).toEqual(true);
  });

  test('check peak trough out of range', () => {
    expect(isPeakTroughInWarning(
      0, mid, startTimeOffsetFromSignalDetection - 0.1, 
      endTimeOffsetFromSignalDetection + 0.1)).toEqual(true);
  });
});

/**
 * Tests the ability to find the [min,max] for the peak trough
 */
describe('findMinMaxForPeakTrough', () => {

  test('find [min,max] with a bad starting index', () => {
    const values = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    
    let result = findMinMaxAmplitudeForPeakTrough(-1, values);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);

    result = findMinMaxAmplitudeForPeakTrough(values.length, values);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);
  });

  test('find [min,max] with a bad data values', () => {

    let result = findMinMaxAmplitudeForPeakTrough(0, undefined);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);

    result = findMinMaxAmplitudeForPeakTrough(0, []);
    expect(result.min.value).toEqual(0);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(0);
    expect(result.max.index).toEqual(0);
  });

  test('find [min,max] for a flat line', () => {
    const values = [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2];
    let result = findMinMaxAmplitudeForPeakTrough(4, values);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(2);
    expect(result.max.index).toEqual(values.length - 1);
  });

  test('find [min,max] for a partial flat line', () => {
    const values = [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5];
    let result = findMinMaxAmplitudeForPeakTrough(4, values);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(5);
    expect(result.max.index).toEqual(values.length - 1);
  });

  test('find [min,max] for another partial flat line', () => {
    const values = [7, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5];
    let result = findMinMaxAmplitudeForPeakTrough(10, values);
    expect(result.min.value).toEqual(2);
    expect(result.min.index).toEqual(1);
    expect(result.max.value).toEqual(5);
    expect(result.max.index).toEqual(values.length - 1);
  });

  test('find [min,max] with good data', () => {
    const values = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 9, 8, 7, 6, 5, 4];
    
    let result = findMinMaxAmplitudeForPeakTrough(0, values);
    expect(result.min.value).toEqual(1);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(9, values);
    expect(result.min.value).toEqual(1);
    expect(result.min.index).toEqual(0);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(13, values);
    expect(result.min.value).toEqual(4);
    expect(result.min.index).toEqual(15);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);

    result = findMinMaxAmplitudeForPeakTrough(15, values);
    expect(result.min.value).toEqual(4);
    expect(result.min.index).toEqual(15);
    expect(result.max.value).toEqual(10);
    expect(result.max.index).toEqual(9);
  });
});

/**
 * Tests the ability to find the [min,max] for the peak trough of a waveform
 */
describe('determineMinMaxForPeakTroughForWaveform', () => {

  test('find [min,max] for a waveform', () => {
    const timeSecs = 1005;
    const values = [2, 3, 4, 5, 6, 7, 8, 9, 10, 4, 3, 2, 1.5];
    const waveform: WaveformTypes.Waveform = {
      sampleCount: values.length,
      sampleRate: 2,
      startTime: 1000,
      values
    }

    const result = determineMinMaxForPeakTroughForWaveform(waveform, timeSecs);
    expect(result.minTimeSecs).toEqual(1006);
    expect(result.min).toEqual(1.5);
    expect(result.maxTimeSecs).toEqual(1004);
    expect(result.max).toEqual(10);
  });
});

/**
 * Tests the ability scale an amplitude measurement value
 */
describe('scaleAmplitudeMeasurementValue', () => {

  test('expect scale of amplitude measurement too throw exception with bad data', () => {
    expect(() => {
      scaleAmplitudeMeasurementValue(undefined);
    }).toThrow();
  });

  test('scale amplitude measurement value', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);

    const amplitudeMeasurementValue: AmplitudeMeasurementValue = {
      amplitude: {
        value: amplitude,
        standardDeviation: 0,
        units: Units.UNITLESS
      },
      period: periodValues[8],
      startTime: 500
    };

    const scaledAmplitudeMeasurementValue = scaleAmplitudeMeasurementValue(amplitudeMeasurementValue);
    const normalizedAmplitude = AMPLITUDE_VALUES[8] / AMPLITUDE_VALUES[348];
    expect(scaledAmplitudeMeasurementValue.amplitude.value).toEqual(amplitude / normalizedAmplitude);
    expect(scaledAmplitudeMeasurementValue.amplitude.standardDeviation).toEqual(0);
    expect(scaledAmplitudeMeasurementValue.amplitude.units).toEqual(Units.UNITLESS);
    expect(scaledAmplitudeMeasurementValue.period).toEqual(periodValues[8]);
    expect(scaledAmplitudeMeasurementValue.startTime).toEqual(500);
  });
});

/**
 * Tests the ability calculate the scaled amplitude
 */
describe('scaleAmplitudeForPeakTrough', () => {

  test('expect calculation to throw exception with bad data', () => {
    expect(() => {
      scaleAmplitudeForPeakTrough(0, 0, 1, [], []);
    }).toThrow();

    expect(() => {
      scaleAmplitudeForPeakTrough(0, 0, 1, [1, 2, 3], [1, 2]);
    }).toThrow();
  });

  test('scale amplitude value appropriately when nonminal calibration period is equal to 1', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);
    const normalizedAmplitude = AMPLITUDE_VALUES[5] / AMPLITUDE_VALUES[348];
    expect(scaleAmplitudeForPeakTrough(amplitude, periodValues[5])).toEqual(amplitude / normalizedAmplitude);
  });

  test('scale amplitude value appropriately with nonminal calibration period is equal to 2', () => {
    const amplitude = 5;
    const periodValues = FREQUENCY_VALUES.map(freq => 1 / freq);
    const nominalCalibrationPeriod = 2;
    const expectedFoundPeriod: { index: number; value: number } = { index: 5, value: 909.090909090909 }
    const expectedNormalizedAmplitude = AMPLITUDE_VALUES[expectedFoundPeriod.index];
    const expectedFoundPeriodForCalibration: { index: number; value: number } = { value: 2.004008016032064, index: 313 }
    const normalizedAmplitude = expectedNormalizedAmplitude / AMPLITUDE_VALUES[expectedFoundPeriodForCalibration.index];

    expect(scaleAmplitudeForPeakTrough(amplitude, periodValues[5], nominalCalibrationPeriod))
      .toEqual(amplitude / normalizedAmplitude);
  });
});
    
/**
 * Tests the ability calculate an Amplitude measurement
 */
describe('calculateAmplitudeMeasurementValue', () => {
  const peakAmplitude = 4;
  const troughAmplitude = 2;
  const peakTime = 4;
  const troughTime = 2;
  const expectedResult: AmplitudeMeasurementValue = {
    amplitude: {
      value: 1,
      standardDeviation: 0,
      units: Units.UNITLESS
    },
    period: 4,
    startTime: Math.min(troughTime, peakTime)
  }
  test('expect calculation to set values to return correct result', () => {
    const result: AmplitudeMeasurementValue = 
      calculateAmplitudeMeasurementValue(peakAmplitude, troughAmplitude, peakTime, troughTime)
  
    expect(result).toEqual(expectedResult);
  });

  test('should recalculate with the expected results', () => {
    const expectedResult: AmplitudeMeasurementValue = {
      amplitude: {
        standardDeviation: 0,
        units: Units.UNITLESS,
        value: 1
      },
      period: 2,
      startTime: 1553022096
    };
    const input = {
      startTime: 1553022096,
      peakAmplitude: 2,
      troughAmplitude: 0,
      peakTime: 1553022096,
      troughTime: 1553022097
    };
    const result = calculateAmplitudeMeasurementValue(input.peakAmplitude,
                                                      input.troughAmplitude,
                                                      input.peakTime,
                                                      input.troughTime);
    expect(result)
      .toBeDefined();

    expect(result)
      .toEqual(expectedResult);
  });
});


/**
 * Tests the ability to get the waveform value for the given waveform data
 */
describe('getWaveformValueForTime', () => {

  const timeSecs = 1005;
  const values = [2, 3, 4, 5, 6, 7, 8, 9, 10, 4, 3, 2, 1.5];
  const undefinedWaveform = undefined;
  const waveform: WaveformTypes.Waveform = {
    sampleCount: values.length,
    sampleRate: 2,
    startTime: 1000,
    values
  }

  const expectedResult: {index: number; value: number} = {
    index: 10,
    value: 3
  };

  test('expect calculation to return undefined when no waveforms are given', () => {
    expect(getWaveformValueForTime(undefinedWaveform, timeSecs)).toBeUndefined;
  });

  test('expect waveform value and index to match expected result', () => {
    expect(getWaveformValueForTime(waveform, timeSecs)).toEqual(expectedResult);
  });
});