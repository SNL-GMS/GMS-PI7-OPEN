import gql from 'graphql-tag';

export const waveformFilterFragment = gql`
fragment WaveformFilterFragment on WaveformFilter {
  id
  name
  description
  filterType
  filterPassBandType
  lowFrequencyHz
  highFrequencyHz
  order
  filterSource
  filterCausality
  zeroPhase
  sampleRate
  sampleRateTolerance
  groupDelaySecs
  validForSampleRate
}
`;

export const waveformFragment = gql`
fragment WaveformFragment on Waveform {
  startTime
  sampleRate
  sampleCount
  values
}
`;

export const filteredChannelSegmentFragment = gql`
fragment FilteredChannelSegmentFragment on FilteredChannelSegment {
  id,
  type
  wfFilterId
  sourceChannelId
  channelId
  startTime
  endTime
  timeseries {
    ...WaveformFragment
  }
}
${waveformFragment}
`;
