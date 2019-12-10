import gql from 'graphql-tag';

export const waveformSegmentsAddedSubscription = gql`
  subscription waveformChannelSegmentsAdded {
    waveformChannelSegmentsAdded {
      channel {
        id
      }
      startTime
      endTime
    }
  }
`;
