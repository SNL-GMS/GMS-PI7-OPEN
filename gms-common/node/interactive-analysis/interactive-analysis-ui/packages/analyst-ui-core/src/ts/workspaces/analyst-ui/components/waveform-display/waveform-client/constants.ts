import { WaveformClientState } from './types';

export const DEFAULT_INITIAL_WAVEFORM_CLIENT_STATE: WaveformClientState = {
  isLoading: false,
  total: 0,
  completed: 0,
  percent: 0,
  description: 'Loading waveforms'
};

export const IS_FETCH_FILTER_WAVEFORMS_LAZY = true;

export const IS_TRANSFER_WAVEFORM_GRAPHQL = true;
