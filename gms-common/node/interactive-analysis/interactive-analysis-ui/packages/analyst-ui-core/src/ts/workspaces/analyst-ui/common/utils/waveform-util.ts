import * as Immutable from 'immutable';
import { systemConfig } from '~analyst-ui/config';
import { WaveformTypes } from '~graphql/';
import { Mode } from '~state/analyst-workspace/types';

/**
 * Returns the waveform for the provided mode.
 *
 * @param mode the mode
 * @param sampleRate the sampleRate of the channel
 * @param defaultWaveformFilters default waveform filters
 *
 * @returns filter of type WaveformFilter
 */
export function getWaveformFilterForMode(
  mode: Mode,
  sampleRate: number,
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): WaveformTypes.WaveformFilter {
  let waveformFilter: WaveformTypes.WaveformFilter;
  if (mode === Mode.MEASUREMENT) {
    waveformFilter = defaultWaveformFilters.find(
      filter =>
        filter.filterType.includes(systemConfig.measurementMode.amplitudeFilter.filterType) &&
        filter.filterPassBandType.includes(systemConfig.measurementMode.amplitudeFilter.filterPassBandType) &&
        filter.lowFrequencyHz === systemConfig.measurementMode.amplitudeFilter.lowFrequencyHz &&
        filter.highFrequencyHz === systemConfig.measurementMode.amplitudeFilter.highFrequencyHz &&
        filter.sampleRate === sampleRate);
  }
  return waveformFilter;
}

/**
 * Returns the selected Waveform Filter based on the mode, station id and
 * the channel filters.
 *
 * @param mode the mode
 * @param id id of channel filter
 * @param sampleRate the sampleRate of the channel
 * @param channelFilters channel filters
 * @param defaultWaveformFilters default waveform filters
 *
 * @returns filter of type WaveformFilter
 */
export function getSelectedWaveformFilter(
  mode: Mode,
  id: string,
  sampleRate: number,
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>,
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): WaveformTypes.WaveformFilter {
  let selectedFilter: WaveformTypes.WaveformFilter;
  if (mode !== Mode.DEFAULT) {
    selectedFilter = getWaveformFilterForMode(mode, sampleRate, defaultWaveformFilters);
  } else {
    selectedFilter = channelFilters.has(id)
      ? channelFilters.get(id)
      : (WaveformTypes.UNFILTERED_FILTER as WaveformTypes.WaveformFilter);
  }
  return selectedFilter;
}
