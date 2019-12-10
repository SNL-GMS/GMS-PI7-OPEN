// tslint:disable
import { getWaveformFilterForMode, getSelectedWaveformFilter } from '~analyst-ui/common/utils/waveform-util';
import { Mode } from '~state/analyst-workspace/types';
import { WaveformTypes } from '~graphql/';
import { systemConfig } from '~analyst-ui/config';
import * as Immutable from 'immutable';

const defaultFilters: WaveformTypes.WaveformFilter[] = 
  [{
    "id": "48fd578e-e428-43ff-9f9e-62598e7e6ce6",
    "name": "HAM FIR BP 0.70-2.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 0.70-2.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 0.7,
    "highFrequencyHz": 2,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 20,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 1.2,
    "validForSampleRate": true
  }, {
    "id": "0351d87a-fde9-43e2-8754-84a1d797fbc1",
    "name": "HAM FIR BP 0.70-2.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 0.70-2.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 0.7,
    "highFrequencyHz": 2,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 40,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 0.6,
    "validForSampleRate": true
  }, {
    "id": "56893621-d9a1-4cfd-830a-13969f9d2aad",
    "name": "HAM FIR BP 1.00-3.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 1.00-3.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 1,
    "highFrequencyHz": 3,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 20,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 1.2,
    "validForSampleRate": true
  }, {
    "id": "8bab76bd-db1a-4226-a151-786577adb3d8",
    "name": "HAM FIR BP 1.00-3.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 1.00-3.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 1,
    "highFrequencyHz": 3,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 40,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 0.6,
    "validForSampleRate": true
  }, {
    "id": "db5e61a0-b8dc-48a7-b56c-5e497925e89c",
    "name": "HAM FIR BP 4.00-8.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 4.00-8.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 4,
    "highFrequencyHz": 8,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 20,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 1.19999999999,
    "validForSampleRate": true
  }, {
    "id": "b92d6ade-3e69-42b6-82e5-f9290f50120f",
    "name": "HAM FIR BP 4.00-8.00 Hz",
    "description": "Hamming FIR Filter Band Pass, 4.00-8.00 Hz",
    "filterType": "FIR_HAMMING",
    "filterPassBandType": "BAND_PASS",
    "lowFrequencyHz": 4,
    "highFrequencyHz": 8,
    "order": 48,
    "filterSource": "SYSTEM",
    "filterCausality": "CAUSAL",
    "zeroPhase": false,
    "sampleRate": 40,
    "sampleRateTolerance": 0.05,
    "groupDelaySecs": 0.6,
    "validForSampleRate": true
  }];

/**
 * Test the ability to get the waveform filter for Mode
 */
describe('getWaveformFilterForMode', () => {
  const sampleRate20 = 20;
  const sampleRate40 = 40;
  
  test('check for getting the filter for DEFAULT mode with no filters', () => {
    expect(getWaveformFilterForMode(Mode.DEFAULT, sampleRate20, [])).toEqual(undefined);
    expect(getWaveformFilterForMode(Mode.DEFAULT, sampleRate40, [])).toEqual(undefined);
  });

  test('check for getting the filter for MEASUREMENT mode with no filters', () => {
    expect(getWaveformFilterForMode(Mode.MEASUREMENT, sampleRate20, [])).toEqual(undefined);
    expect(getWaveformFilterForMode(Mode.MEASUREMENT, sampleRate40, [])).toEqual(undefined);

  });

  test('check for getting the filter for MEASUREMENT mode for sample rate of 20', () => {  
    const filter = getWaveformFilterForMode(Mode.MEASUREMENT, sampleRate20, defaultFilters)
    expect(filter.filterType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterType);
    expect(filter.filterPassBandType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterPassBandType);
    expect(filter.lowFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz);
    expect(filter.highFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.highFrequencyHz);
    expect(filter.sampleRate).toEqual(sampleRate20);
  });

  test('check for getting the filter for MEASUREMENT mode for sample rate of 40', () => {  
    const filter = getWaveformFilterForMode(Mode.MEASUREMENT, sampleRate40, defaultFilters)
    expect(filter.filterType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterType);
    expect(filter.filterPassBandType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterPassBandType);
    expect(filter.lowFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz);
    expect(filter.highFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.highFrequencyHz);
    expect(filter.sampleRate).toEqual(sampleRate40);
  });

});

/**
 * Test the ability to get the selected waveform filter
 */
describe('getSelectedWaveformFilter', () => {
  const sampleRate20 = 20;
  const sampleRate40 = 40;

  const id = 'MyId';
  const channelFilter = defaultFilters[1]
  let channelFilters = Immutable.Map<string, WaveformTypes.WaveformFilter>();
  channelFilters = channelFilters.set(id, channelFilter);

  test('check for getting the filter for MEASURMENT mode for sample rate 20', () => {
    const filter = getSelectedWaveformFilter(Mode.MEASUREMENT, undefined, sampleRate20, Immutable.Map(), defaultFilters);
    expect(filter.filterType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterType);
    expect(filter.filterPassBandType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterPassBandType);
    expect(filter.lowFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz);
    expect(filter.highFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.highFrequencyHz);
    expect(filter.sampleRate).toEqual(sampleRate20);
  });

  test('check for getting the filter for MEASURMENT mode for sample rate 40', () => {
    const filter = getSelectedWaveformFilter(Mode.MEASUREMENT, undefined, sampleRate40, Immutable.Map(), defaultFilters);
    expect(filter.filterType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterType);
    expect(filter.filterPassBandType).toEqual(systemConfig.measurementMode.amplitudeFilter.filterPassBandType);
    expect(filter.lowFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz);
    expect(filter.highFrequencyHz).toEqual(systemConfig.measurementMode.amplitudeFilter.highFrequencyHz);
    expect(filter.sampleRate).toEqual(sampleRate40);
  });

  test('check for getting the filter for DEFAULT mode with no channel filters for sample rate 20', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, undefined, sampleRate20, Immutable.Map(), defaultFilters);
    expect(filter).toEqual(WaveformTypes.UNFILTERED_FILTER);
  });

  test('check for getting the filter for DEFAULT mode with no channel filters for sample rate 40', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, undefined, sampleRate40, Immutable.Map(), defaultFilters);
    expect(filter).toEqual(WaveformTypes.UNFILTERED_FILTER);
  });

  test('check for getting the filter for DEFAULT mode with bad id for sample rate 20', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, 'BadId', sampleRate20, channelFilters, defaultFilters);
    expect(filter).toEqual(WaveformTypes.UNFILTERED_FILTER);
  });

  test('check for getting the filter for DEFAULT mode with bad id for sample rate 40', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, 'BadId', sampleRate40, channelFilters, defaultFilters);
    expect(filter).toEqual(WaveformTypes.UNFILTERED_FILTER);
  });

  test('check for getting the filter for DEFAULT mode with channel filters for sample rate 20', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, id, sampleRate20, channelFilters, defaultFilters);
    expect(filter).toEqual(channelFilter);
  });

  test('check for getting the filter for DEFAULT mode with channel filters for sample rate 40', () => {
    const filter = getSelectedWaveformFilter(Mode.DEFAULT, id, sampleRate40, channelFilters, defaultFilters);
    expect(filter).toEqual(channelFilter);
  });

});