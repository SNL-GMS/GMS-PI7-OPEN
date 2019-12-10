import { GraphqlQueryControls } from 'react-apollo';
import { ChannelSegment, TimeSeries } from '~graphql/channel-segment/types';

// ***************************************
// Mutations
// ***************************************

// ***************************************
// Subscriptions
// ***************************************

export interface WaveformSegmentsAddedSubscription {
  waveformChannelSegmentsAdded: {
    channel: {
        id: string;
    };
    startTime: number;
    endTime: number;
  }[];
}

// ***************************************
// Queries
// ***************************************

export interface GetWaveformSegmentQueryArgs {
  timeRange: {
    startTime: number;
    endTime: number;
  };
  channelIds: string[];
  filterIds?: string[];
}

export interface GetRawWaveformSegmentQueryArgs {
  timeRange: {
    startTime: number;
    endTime: number;
  };
  channelIds: string[];
}

export interface GetFilteredWaveformSegmentQueryArgs {
  timeRange: {
    startTime: number;
    endTime: number;
  };
  channelIds: string[];
  filterIds?: string[];
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type DefaultWaveformFiltersQueryProps =  { defaultWaveformFiltersQuery: GraphqlQueryControls<{}> & {defaultWaveformFilters: WaveformFilter[]}};

// ***************************************
// Model
// ***************************************

export interface Waveform extends TimeSeries {
  values: number[];
}

export interface FilteredChannelSegment extends ChannelSegment<Waveform> {
  sourceChannelId: string;
  wfFilterId: string;
}

export interface RawAndFilteredChannelSegments {
  channelId: string;
  raw: ChannelSegment<Waveform>[];
  filtered: FilteredChannelSegment[];
}

export interface WaveformFilter {
  id: string;
  name: string;
  description: string;
  filterType: string; // FIR_HAMMING
  filterPassBandType: string; // BAND_PASS, HIGH_PASS
  lowFrequencyHz: number;
  highFrequencyHz: number;
  order: number;
  filterSource: string; // SYSTEM
  filterCausality: string; // CAUSAL
  zeroPhase: boolean;
  sampleRate: number;
  sampleRateTolerance: number;
  validForSampleRate: boolean;
  aCoefficients?: number[];
  bCoefficients?: number[];
  groupDelaySecs: number;
}

export const DEFAULT_SAMPLE_RATE = 1;

export const UNFILTERED = 'unfiltered';

export const UNFILTERED_FILTER: Partial<WaveformFilter> = {
  id: UNFILTERED,
  name: UNFILTERED,
  sampleRate: undefined
};
